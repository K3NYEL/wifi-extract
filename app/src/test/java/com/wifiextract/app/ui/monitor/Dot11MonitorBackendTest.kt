package com.wifiextract.app.ui.monitor

import org.junit.Assert.*
import org.junit.Test

private class FakeProvider(var failOpen: Boolean = false) : CaptureProvider {
    override val name = "Fake"
    var sink: ((ByteArray) -> Unit)? = null
    var closed = 0
    override fun availability(context: android.content.Context) = ProviderAvailability(true, "listo")
    override fun open(sink: (ByteArray) -> Unit) { if (failOpen) error("boom"); this.sink = sink }
    override fun close() { closed++ }
}

private class StubFallback : MonitorBackend {
    override fun inspect(context: android.content.Context) = base
    override fun start(onFrame: (MonitorFrame) -> Unit) = MonitorStartResult.Unavailable("sin proveedor")
    override fun stop() = Unit
}

private val base = MonitorCapabilities(true, "14", false, "stub", "x")
private val OK = ProviderAvailability(true, "listo")
private val NO = ProviderAvailability(false, "no soportado")

class Dot11MonitorBackendTest {
    private val beaconRt = byteArrayOf(0, 0, 15, 0, 0x2a, 0, 0, 0, 0, 0, 0x85.toByte(), 0x09, 0xa0.toByte(), 0, 0xd6.toByte()) +
        ("80 00 00 00 ff ff ff ff ff ff 02 11 22 33 44 55 02 11 22 33 44 55 10 00 " +
            "00 00 00 00 00 00 00 00 64 00 00 00 00 04 54 65 73 74").split(" ").map { it.toInt(16).toByte() }.toByteArray()

    @Test fun withoutProviderRefusesToStart() {
        val b = Dot11MonitorBackend(null, fallback = StubFallback())
        assertTrue(b.start { } is MonitorStartResult.Unavailable)
        assertFalse(b.applyAvailability(base, null).rawFrameCaptureAvailable)
    }

    @Test fun mustInspectBeforeStart() {
        val b = Dot11MonitorBackend(FakeProvider(), fallback = StubFallback())
        assertTrue(b.start { } is MonitorStartResult.Unavailable)
    }

    @Test fun unsupportedProviderIsNotStarted() {
        val p = FakeProvider()
        val b = Dot11MonitorBackend(p, fallback = StubFallback())
        assertFalse(b.applyAvailability(base, NO).rawFrameCaptureAvailable)
        assertTrue(b.start { } is MonitorStartResult.Unavailable)
        assertNull(p.sink)
    }

    @Test fun deliversParsedFramesAndDropsGarbage() {
        val p = FakeProvider()
        val got = ArrayList<MonitorFrame>()
        val b = Dot11MonitorBackend(p, clock = { 123L }, fallback = StubFallback())
        assertTrue(b.applyAvailability(base, OK).rawFrameCaptureAvailable)
        assertTrue(b.start { got.add(it) } is MonitorStartResult.Started)
        p.sink!!(beaconRt); p.sink!!(ByteArray(3))
        assertEquals(1, got.size)
        assertEquals(123L, got[0].timestampMillis); assertEquals(6, got[0].channel); assertEquals(-42, got[0].signalDbm)
        assertEquals(1, b.api.stats().framesRejected)
        b.stop(); assertEquals(1, p.closed)
    }

    @Test fun providerFailureIsReported() {
        val p = FakeProvider(failOpen = true)
        val b = Dot11MonitorBackend(p, fallback = StubFallback())
        b.applyAvailability(base, OK)
        assertTrue(b.start { } is MonitorStartResult.Failed)
        assertEquals(1, p.closed)
    }
}
