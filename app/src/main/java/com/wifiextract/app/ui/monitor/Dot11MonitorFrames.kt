package com.wifiextract.app.ui.monitor

import com.wifiextract.app.core.dot11.Dot11Frame

/** Adapts a parsed frame to the monitor backend contract, which carries metadata only. */
fun Dot11Frame.toMonitorFrame(timestampMillis: Long): MonitorFrame = MonitorFrame(
    timestampMillis = timestampMillis,
    channel = effectiveChannel,
    frameType = "$type/$subtypeName",
    signalDbm = radio?.signalDbm
)
