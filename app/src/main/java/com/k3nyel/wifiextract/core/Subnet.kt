package com.k3nyel.wifiextract.core

/** IPv4 subnet helpers. Scans are bounded to at most a /24 (254 hosts). */
object Subnet {
    fun maskString(prefix: Int): String {
        if (prefix !in 1..32) return ""
        return toDotted(-1 shl (32 - prefix))
    }

    /** Hosts of the subnet containing [address], excluding network, broadcast and the own address. */
    fun hosts(address: ByteArray, prefix: Int): List<String> {
        require(address.size == 4) { "IPv4 address expected" }
        val effective = prefix.coerceIn(24, 30)
        val ip = address.fold(0) { acc, b -> (acc shl 8) or (b.toInt() and 0xFF) }
        val network = ip and (-1 shl (32 - effective))
        val size = 1 shl (32 - effective)
        val own = toDotted(ip)
        return (1 until size - 1).map { toDotted(network + it) }.filter { it != own }
    }

    fun ipToLong(ip: String): Long =
        ip.split('.').fold(0L) { acc, part -> (acc shl 8) or (part.toLongOrNull() ?: 0L) }

    private fun toDotted(v: Int): String =
        "${(v ushr 24) and 0xFF}.${(v ushr 16) and 0xFF}.${(v ushr 8) and 0xFF}.${v and 0xFF}"
}
