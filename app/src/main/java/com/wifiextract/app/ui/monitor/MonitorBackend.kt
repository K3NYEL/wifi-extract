@file:Suppress("unused")

package com.wifiextract.app.ui.monitor

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/**
 * Metadata-only representation of a received 802.11 frame.
 *
 * A backend must never emit fabricated frames. The platform backend below does not
 * support raw-frame capture; this model is the contract for a future compatible driver.
 */
data class MonitorFrame(
    val timestampMillis: Long,
    val channel: Int?,
    val frameType: String,
    val signalDbm: Int?
)

data class MonitorCapabilities(
    val wifiHardwareDetected: Boolean,
    val androidVersion: String,
    val rawFrameCaptureAvailable: Boolean,
    val providerName: String,
    val explanation: String
)

sealed interface MonitorStartResult {
    data object Started : MonitorStartResult
    data class Unavailable(val reason: String) : MonitorStartResult
    data class Failed(val reason: String) : MonitorStartResult
}

/**
 * Contract for a genuine passive 802.11 capture provider.
 * Implementations may report received frame metadata, but must not inject or transmit frames.
 */
interface MonitorBackend {
    fun inspect(context: Context): MonitorCapabilities
    fun start(onFrame: (MonitorFrame) -> Unit): MonitorStartResult
    fun stop()
}

/**
 * Safe default for ordinary Android installations.
 *
 * Android's public application APIs do not expose monitor-mode control or raw 802.11
 * frame delivery. This backend therefore reports the limitation and refuses to start;
 * it does not substitute Wi-Fi scan results or generated sample data.
 */
class AndroidPublicApiMonitorBackend : MonitorBackend {
    override fun inspect(context: Context): MonitorCapabilities {
        val hasWifi = context.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI)
        val androidVersion = Build.VERSION.RELEASE
            ?: Build.VERSION.SDK_INT.toString()

        return MonitorCapabilities(
            wifiHardwareDetected = hasWifi,
            androidVersion = androidVersion,
            rawFrameCaptureAvailable = false,
            providerName = "Android Public API",
            explanation = if (hasWifi) {
                "El dispositivo tiene Wi-Fi, pero Android no ofrece a esta app una API pública para activar monitor mode ni recibir tramas 802.11 sin procesar. No hay un proveedor de captura compatible disponible."
            } else {
                "No se detectó la característica de hardware Wi-Fi y tampoco existe un proveedor de captura 802.11 disponible."
            }
        )
    }

    override fun start(onFrame: (MonitorFrame) -> Unit): MonitorStartResult =
        MonitorStartResult.Unavailable(
            "Captura 802.11 no disponible: falta un proveedor compatible con el chipset y el controlador del dispositivo."
        )

    override fun stop() = Unit
}
