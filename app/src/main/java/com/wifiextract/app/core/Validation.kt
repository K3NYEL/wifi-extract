package com.wifiextract.app.core

/** Input validation for user-supplied hosts and ports. IPv6 literals are not supported yet. */
object Validation {
    private val ipv4 = Regex("^((25[0-5]|2[0-4]\\d|1?\\d?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1?\\d?\\d)$")
    private val hostname =
        Regex("^[A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?(\\.[A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?)*$")

    fun isValidHost(value: String): Boolean {
        val v = value.trim()
        return v.length in 1..253 && (ipv4.matches(v) || hostname.matches(v))
    }

    fun parsePort(value: String): Int? = value.trim().toIntOrNull()?.takeIf { it in 1..65535 }
}
