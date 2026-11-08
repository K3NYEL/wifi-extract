package com.wifiextract.app.core

import java.text.NumberFormat
import java.util.Locale

object Formatters {

    fun formatBytes(bytes: Long, locale: Locale = Locale.getDefault()): String {
        if (bytes < 1024) return "$bytes B"
        val exp = (Math.log(bytes.toDouble()) / Math.log(1024.0)).toInt().coerceAtMost(4)
        val pre = "KMGT"[exp - 1]
        val value = bytes / Math.pow(1024.0, exp.toDouble())
        val nf = NumberFormat.getNumberInstance(locale).apply {
            maximumFractionDigits = 1
            minimumFractionDigits = 0
        }
        return "${nf.format(value)} ${pre}B"
    }

    fun formatDistanceMeters(meters: Double, locale: Locale = Locale.getDefault()): String {
        val nf = NumberFormat.getNumberInstance(locale).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 0
        }
        return "${nf.format(meters)} m"
    }

    fun formatPercent(percent: Int, locale: Locale = Locale.getDefault()): String {
        val nf = NumberFormat.getNumberInstance(locale)
        return "${nf.format(percent)}%"
    }
}
