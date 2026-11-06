package com.wifiextract.app.ui.monitor

import android.content.Context
import com.wifiextract.app.core.dot11.Dot11FrameApi
import com.wifiextract.app.core.dot11.Dot11ParseResult

/** Whether a capture provider can work on this device, with a user-facing reason when it cannot. */
data class ProviderAvailability(val available: Boolean, val reason: String)

/**
 * Source of raw radiotap-framed 802.11 captures (for example a vendor driver or an explicitly
 * supported external adapter). Implementations must be receive-only.
 */
interface CaptureProvider {
    val name: String
    fun availability(context: Context): ProviderAvailability

    /** Starts delivering radiotap captures to [sink]. Throws on failure; never fabricates data. */
    fun open(sink: (ByteArray) -> Unit)
    fun close()
}

/**
 * [MonitorBackend] that connects a [CaptureProvider] to the 802.11 frame API.
 *
 * Without a provider (the default on stock Android) it behaves exactly like
 * [AndroidPublicApiMonitorBackend]: it reports the limitation and refuses to start.
 */
class Dot11MonitorBackend(
    private val provider: CaptureProvider? = null,
    val api: Dot11FrameApi = Dot11FrameApi(),
    private val clock: () -> Long = System::currentTimeMillis,
    private val fallback: MonitorBackend = AndroidPublicApiMonitorBackend()
) : MonitorBackend {
    @Volatile private var lastAvailability: ProviderAvailability? = null
    @Volatile private var running = false

    override fun inspect(context: Context): MonitorCapabilities =
        applyAvailability(fallback.inspect(context), provider?.availability(context))

    /** Separated from [inspect] so it can be unit-tested without an Android [Context]. */
    internal fun applyAvailability(base: MonitorCapabilities, availability: ProviderAvailability?): MonitorCapabilities {
        val p = provider ?: return base
        val a = availability ?: ProviderAvailability(false, "Proveedor sin información de disponibilidad.")
        lastAvailability = a
        return base.copy(rawFrameCaptureAvailable = a.available, providerName = p.name, explanation = a.reason)
    }

    override fun start(onFrame: (MonitorFrame) -> Unit): MonitorStartResult {
        val p = provider ?: return fallback.start(onFrame)
        if (running) return MonitorStartResult.Failed("La captura ya está en curso.")
        if (lastAvailability?.available != true) {
            return MonitorStartResult.Unavailable(
                lastAvailability?.reason ?: "Comprueba la disponibilidad del proveedor antes de iniciar."
            )
        }
        return try {
            api.clear()
            p.open { raw ->
                // Malformed captures are counted by the API and dropped; they never reach the UI.
                val r = api.ingestRadiotap(raw)
                if (r is Dot11ParseResult.Parsed) onFrame(r.frame.toMonitorFrame(clock()))
            }
            running = true
            MonitorStartResult.Started
        } catch (e: Exception) {
            runCatching { p.close() }
            MonitorStartResult.Failed("No se pudo iniciar la captura: ${e.javaClass.simpleName}")
        }
    }

    override fun stop() {
        if (running) runCatching { provider?.close() }
        running = false
        fallback.stop()
    }
}
