package com.wifiextract.app.core.dot11

import org.junit.Assert.*
import org.junit.Test

private fun hex(s: String) = s.replace(" ", "").chunked(2).map { it.toInt(16).toByte() }.toByteArray()


class Dot11Test {
    private fun radiotap(fcs: Boolean): ByteArray {
        // header 8 + flags(1) + pad(1) + channel(4) + signal(1) = 15 -> len 15
        val flags = if (fcs) 0x10 else 0x00
        return byteArrayOf(0, 0, 15, 0, 0x2a, 0, 0, 0, flags.toByte(), 0, 0x85.toByte(), 0x09, 0xa0.toByte(), 0, 0xd6.toByte())
    }

    private val beacon = hex(
        "80 00 00 00 ff ff ff ff ff ff 02 11 22 33 44 55 02 11 22 33 44 55 10 00" +
            "00 00 00 00 00 00 00 00 64 00 11 00" +            // timestamp, interval, capability (privacy)
            "00 04 54 65 73 74 03 01 06" +                      // SSID "Test", channel 6
            "30 14 01 00 00 0f ac 04 01 00 00 0f ac 04 01 00 00 0f ac 02 00 00" // RSN, PSK
    )

    @Test fun parsesBeacon() {
        val r = Dot11Parser.parse(beacon) as Dot11ParseResult.Parsed
        val f = r.frame
        assertEquals(FrameType.MANAGEMENT, f.type)
        assertEquals("Beacon", f.subtypeName)
        assertEquals("Test", f.ssid)
        assertEquals(6, f.advertisedChannel)
        assertEquals(SecurityType.WPA2, f.security)
        assertTrue(f.receiver!!.isBroadcast)
        assertEquals("02:11:22:33:44:55", f.bssid.toString())
        assertEquals(1, f.sequenceNumber)
    }

    @Test fun radiotapMetadataAndFcsStripped() {
        val raw = radiotap(true) + beacon + byteArrayOf(1, 2, 3, 4)
        val f = (Dot11Parser.parseRadiotap(raw) as Dot11ParseResult.Parsed).frame
        assertEquals(2437, f.radio!!.frequencyMhz)
        assertEquals(6, f.radio!!.channel)
        assertEquals(Band.GHZ_2_4, f.radio!!.band)
        assertEquals(-42, f.radio!!.signalDbm)
        assertEquals(beacon.size, f.capturedLength)
        assertEquals(6, f.effectiveChannel)
    }

    @Test fun hiddenSsidAndOpenNetwork() {
        val b = beacon.copyOf(36) + hex("00 00 03 01 0b")
        val f = (Dot11Parser.parse(b) as Dot11ParseResult.Parsed).frame
        assertTrue(f.ssidHidden); assertNull(f.ssid); assertEquals(11, f.advertisedChannel)
        assertEquals(SecurityType.WEP, f.security) // privacy bit set, no RSN
    }

    @Test fun controlAckHasNoTransmitter() {
        val f = (Dot11Parser.parse(hex("d4 00 00 00 02 11 22 33 44 55")) as Dot11ParseResult.Parsed).frame
        assertEquals("ACK", f.subtypeName); assertNull(f.transmitter)
    }

    @Test fun dataFrameBssidFollowsDsBits() {
        val h = hex("08 01 00 00 aa aa aa aa aa aa bb bb bb bb bb bb cc cc cc cc cc cc 20 00") // toDS
        val f = (Dot11Parser.parse(h) as Dot11ParseResult.Parsed).frame
        assertEquals(FrameType.DATA, f.type); assertEquals("aa:aa:aa:aa:aa:aa", f.bssid.toString())
        assertEquals(2, f.sequenceNumber)
    }

    @Test fun rejectsGarbageWithoutThrowing() {
        assertTrue(Dot11Parser.parse(ByteArray(0)) is Dot11ParseResult.Malformed)
        assertTrue(Dot11Parser.parse(hex("80 00 00")) is Dot11ParseResult.Malformed)
        assertTrue(Dot11Parser.parse(hex("81 00 00 00 ff ff ff ff ff ff")) is Dot11ParseResult.Malformed)
        assertTrue(Dot11Parser.parseRadiotap(hex("01 00 08 00 00 00 00 00")) is Dot11ParseResult.Malformed)
        assertTrue(Dot11Parser.parseRadiotap(hex("00 00 ff 00 00 00 00 00")) is Dot11ParseResult.Malformed)
        assertTrue(Dot11Parser.parse(ByteArray(10_000)) is Dot11ParseResult.Malformed)
        val rnd = java.util.Random(7)
        repeat(5000) { Dot11Parser.parse(ByteArray(rnd.nextInt(120)).also(rnd::nextBytes)); Dot11Parser.parseRadiotap(ByteArray(rnd.nextInt(120)).also(rnd::nextBytes)) }
    }

    @Test fun truncatedInformationElementIsIgnored() {
        val f = (Dot11Parser.parse(beacon.copyOf(beacon.size - 5)) as Dot11ParseResult.Parsed).frame
        assertEquals("Test", f.ssid); assertEquals(SecurityType.WEP, f.security)
    }

    @Test fun ssidControlCharactersAreStripped() {
        val b = beacon.copyOf(36) + hex("00 03 41 0a 42")
        assertEquals("AB", ((Dot11Parser.parse(b) as Dot11ParseResult.Parsed).frame).ssid)
    }

    @Test fun apiTracksNetworksStatsAndBounds() {
        val api = Dot11FrameApi(capacity = 2)
        assertTrue(api.frames().isEmpty())
        repeat(3) { api.ingest(beacon) }
        api.ingest(ByteArray(0))
        val s = api.stats()
        assertEquals(3, s.framesAccepted); assertEquals(1, s.framesRejected)
        assertEquals(2, api.frames().size)
        assertEquals(1, s.networks.size); assertEquals(3, s.networks[0].beacons)
        assertEquals(1, api.frames(FrameFilter(channel = 6)).size.coerceAtMost(1))
        api.clear(); assertEquals(0, api.stats().framesAccepted)
    }

    @Test fun macRedaction() {
        assertEquals("02:11:22:xx:xx:xx", MacAddress(0x021122334455L).redacted())
        assertTrue(MacAddress(0x021122334455L).isLocallyAdministered)
    }

    @Test fun channelMapping() {
        assertEquals(14, Radiotap.channelFor(2484).first)
        assertEquals(36, Radiotap.channelFor(5180).first)
        assertEquals(Band.GHZ_6, Radiotap.channelFor(5955).second)
        assertNull(Radiotap.channelFor(1234).first)
    }
}
