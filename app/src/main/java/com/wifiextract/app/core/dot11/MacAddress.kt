package com.wifiextract.app.core.dot11

/** IEEE 802 48-bit address. Stored as a Long so frames stay small and comparable. */
@JvmInline
value class MacAddress(val raw: Long) {
    val isBroadcast: Boolean get() = raw == BROADCAST_RAW
    val isMulticast: Boolean get() = (raw shr 40) and 0x01L == 1L
    val isLocallyAdministered: Boolean get() = (raw shr 40) and 0x02L == 2L

    override fun toString(): String = format(masked = false)

    /** Keeps the vendor prefix (OUI) and hides the device-specific half, for logs and exports. */
    fun redacted(): String = format(masked = true)

    private fun format(masked: Boolean): String =
        (5 downTo 0).joinToString(":") { i ->
            if (masked && i < 3) "xx" else "%02x".format(((raw shr (i * 8)) and 0xFF).toInt())
        }

    companion object {
        private const val BROADCAST_RAW = 0xFFFFFFFFFFFFL

        fun fromBytes(bytes: ByteArray, offset: Int): MacAddress {
            require(offset >= 0 && offset + 6 <= bytes.size) { "MAC out of range" }
            var v = 0L
            for (i in 0 until 6) v = (v shl 8) or (bytes[offset + i].toLong() and 0xFF)
            return MacAddress(v)
        }
    }
}
