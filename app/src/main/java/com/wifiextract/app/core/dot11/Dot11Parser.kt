package com.wifiextract.app.core.dot11

/**
 * Passive parser for received 802.11 frames. It only reads; it cannot build or transmit frames.
 * It never throws on hostile input: every failure is reported as [Dot11ParseResult.Malformed].
 */
object Dot11Parser {
    const val MAX_FRAME_BYTES = 4096 + 512
    private const val MAX_IE_COUNT = 64

    /** Parses a capture that starts with a radiotap header (the usual monitor-mode link type 127). */
    fun parseRadiotap(bytes: ByteArray): Dot11ParseResult {
        if (bytes.isEmpty()) return Dot11ParseResult.Malformed(MalformedReason.EMPTY)
        if (bytes.size > MAX_FRAME_BYTES) return Dot11ParseResult.Malformed(MalformedReason.FRAME_TOO_LARGE)
        val (hdr, err) = Radiotap.parse(bytes)
        if (hdr == null) return Dot11ParseResult.Malformed(err!!)
        val end = if (hdr.fcsPresent) bytes.size - 4 else bytes.size
        if (end <= hdr.length) return Dot11ParseResult.Malformed(MalformedReason.FRAME_TRUNCATED)
        return parseDot11(bytes, hdr.length, end, hdr.radio)
    }

    /** Parses a bare 802.11 frame (no radiotap, no FCS). */
    fun parse(bytes: ByteArray, radio: RadioInfo? = null): Dot11ParseResult {
        if (bytes.isEmpty()) return Dot11ParseResult.Malformed(MalformedReason.EMPTY)
        if (bytes.size > MAX_FRAME_BYTES) return Dot11ParseResult.Malformed(MalformedReason.FRAME_TOO_LARGE)
        return parseDot11(bytes, 0, bytes.size, radio)
    }

    private fun parseDot11(b: ByteArray, start: Int, end: Int, radio: RadioInfo?): Dot11ParseResult {
        fun bad(r: MalformedReason) = Dot11ParseResult.Malformed(r)
        if (end - start < 10) return bad(MalformedReason.FRAME_TRUNCATED)

        val fc0 = b[start].toInt() and 0xFF
        val fc1 = b[start + 1].toInt() and 0xFF
        if (fc0 and 0x03 != 0) return bad(MalformedReason.UNSUPPORTED_PROTOCOL_VERSION)
        val type = when ((fc0 shr 2) and 0x03) {
            0 -> FrameType.MANAGEMENT; 1 -> FrameType.CONTROL; 2 -> FrameType.DATA; else -> FrameType.EXTENSION
        }
        val subtype = (fc0 shr 4) and 0x0F
        val flags = FrameFlags(
            toDs = fc1 and 0x01 != 0, fromDs = fc1 and 0x02 != 0, moreFragments = fc1 and 0x04 != 0,
            retry = fc1 and 0x08 != 0, powerManagement = fc1 and 0x10 != 0, moreData = fc1 and 0x20 != 0,
            protectedFrame = fc1 and 0x40 != 0, order = fc1 and 0x80 != 0
        )

        val addr1 = MacAddress.fromBytes(b, start + 4)
        var addr2: MacAddress? = null
        var addr3: MacAddress? = null
        var seq: Int? = null
        var bodyStart = start + 10
        var bssid: MacAddress? = null
        var transmitter: MacAddress? = null

        when (type) {
            FrameType.MANAGEMENT -> {
                if (end - start < 24) return bad(MalformedReason.FRAME_TRUNCATED)
                addr2 = MacAddress.fromBytes(b, start + 10)
                addr3 = MacAddress.fromBytes(b, start + 16)
                seq = Radiotap.u16(b, start + 22) shr 4
                bodyStart = start + 24 + if (flags.order) 4 else 0
                transmitter = addr2; bssid = addr3
            }
            FrameType.CONTROL -> {
                // RTS, PS-Poll, CF-End, BlockAckReq, BlockAck carry a transmitter address.
                if (subtype in 8..11 || subtype == 14 || subtype == 15) {
                    if (end - start < 16) return bad(MalformedReason.FRAME_TRUNCATED)
                    addr2 = MacAddress.fromBytes(b, start + 10)
                    transmitter = addr2
                }
            }
            FrameType.DATA -> {
                if (end - start < 24) return bad(MalformedReason.FRAME_TRUNCATED)
                addr2 = MacAddress.fromBytes(b, start + 10)
                addr3 = MacAddress.fromBytes(b, start + 16)
                seq = Radiotap.u16(b, start + 22) shr 4
                transmitter = addr2
                bssid = when {
                    !flags.toDs && !flags.fromDs -> addr3
                    !flags.toDs && flags.fromDs -> addr2
                    flags.toDs && !flags.fromDs -> addr1
                    else -> null // WDS: no BSSID field
                }
            }
            FrameType.EXTENSION -> Unit
        }

        var ssid: String? = null
        var hidden = false
        var channel: Int? = null
        var security = SecurityType.UNKNOWN

        if (type == FrameType.MANAGEMENT) {
            // Fixed-parameter length before the information elements.
            val ieStart = when (subtype) {
                8, 5 -> bodyStart + 12      // beacon, probe response
                4 -> bodyStart              // probe request
                0 -> bodyStart + 4          // association request
                2 -> bodyStart + 10         // reassociation request
                else -> -1
            }
            if (ieStart in 0..end) {
                val ie = parseElements(b, ieStart, end)
                ssid = ie.ssid; hidden = ie.hidden; channel = ie.channel
                if (subtype == 8 || subtype == 5) {
                    val cap = if (bodyStart + 12 <= end) Radiotap.u16(b, bodyStart + 10) else 0
                    security = classify(ie, privacy = cap and 0x0010 != 0)
                }
            }
        }

        return Dot11ParseResult.Parsed(
            Dot11Frame(
                type, subtype, subtypeName(type, subtype), flags, addr1, transmitter, bssid, seq,
                ssid, hidden, channel, security, radio, end - start
            )
        )
    }

    private class Elements(
        var ssid: String? = null, var hidden: Boolean = false, var channel: Int? = null,
        var rsn: Boolean = false, var wpa: Boolean = false, var sae: Boolean = false, var psk: Boolean = false
    )

    private fun parseElements(b: ByteArray, from: Int, end: Int): Elements {
        val out = Elements()
        var o = from
        var count = 0
        while (o + 2 <= end && count++ < MAX_IE_COUNT) {
            val id = b[o].toInt() and 0xFF
            val len = b[o + 1].toInt() and 0xFF
            val d = o + 2
            if (d + len > end) break
            when (id) {
                0 -> if (len <= 32) {
                    if (len == 0 || (d until d + len).all { b[it].toInt() == 0 }) out.hidden = true
                    else out.ssid = sanitize(String(b, d, len, Charsets.UTF_8))
                }
                3 -> if (len == 1) out.channel = b[d].toInt() and 0xFF
                48 -> { out.rsn = true; parseRsn(b, d, len, out) }
                221 -> if (len >= 4 && b[d].toInt() == 0x00 && b[d + 1].toInt() == 0x50 &&
                    (b[d + 2].toInt() and 0xFF) == 0xF2 && b[d + 3].toInt() == 1) out.wpa = true
            }
            o = d + len
        }
        return out
    }

    private fun parseRsn(b: ByteArray, d: Int, len: Int, out: Elements) {
        val e = d + len
        var o = d + 2 + 4                       // version + group cipher
        if (o + 2 > e) return
        o += 2 + Radiotap.u16(b, o) * 4         // pairwise ciphers
        if (o + 2 > e) return
        val n = Radiotap.u16(b, o); o += 2
        for (i in 0 until minOf(n, 16)) {
            if (o + 4 > e) return
            val isStd = b[o].toInt() == 0x00 && b[o + 1].toInt() == 0x0F && (b[o + 2].toInt() and 0xFF) == 0xAC
            val t = b[o + 3].toInt() and 0xFF
            if (isStd && (t == 8 || t == 9)) out.sae = true
            if (isStd && (t == 2 || t == 4 || t == 6)) out.psk = true
            if (isStd && (t == 1 || t == 3 || t == 5)) out.psk = true // 802.1X still counts as WPA2-class
            o += 4
        }
    }

    private fun classify(e: Elements, privacy: Boolean): SecurityType = when {
        e.rsn && e.sae && e.psk -> SecurityType.WPA2_WPA3
        e.rsn && e.sae -> SecurityType.WPA3
        e.rsn -> SecurityType.WPA2
        e.wpa -> SecurityType.WPA
        privacy -> SecurityType.WEP
        else -> SecurityType.OPEN
    }

    /** SSIDs are attacker-controlled text: drop control characters before they reach any UI or log. */
    private fun sanitize(s: String): String = s.filter { !it.isISOControl() }.take(32)

    private fun subtypeName(t: FrameType, s: Int): String = when (t) {
        FrameType.MANAGEMENT -> listOf(
            "Association Request", "Association Response", "Reassociation Request", "Reassociation Response",
            "Probe Request", "Probe Response", "Timing Advertisement", "Reserved", "Beacon", "ATIM",
            "Disassociation", "Authentication", "Deauthentication", "Action", "Action No Ack", "Reserved"
        )[s]
        FrameType.CONTROL -> when (s) {
            4 -> "Beamforming Report Poll"; 5 -> "VHT NDP Announcement"; 7 -> "Control Wrapper"
            8 -> "Block Ack Request"; 9 -> "Block Ack"; 10 -> "PS-Poll"; 11 -> "RTS"
            12 -> "CTS"; 13 -> "ACK"; 14 -> "CF-End"; 15 -> "CF-End + CF-Ack"; else -> "Reserved"
        }
        FrameType.DATA -> when (s) {
            0 -> "Data"; 4 -> "Null"; 8 -> "QoS Data"; 12 -> "QoS Null"; else -> "Data (subtype $s)"
        }
        FrameType.EXTENSION -> "Extension"
    }
}
