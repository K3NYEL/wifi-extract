package com.wifiextract.app.core.dot11

/** Result of reading a radiotap header: metadata plus where the 802.11 frame starts/ends. */
internal data class RadiotapHeader(
    val length: Int,
    val radio: RadioInfo,
    val fcsPresent: Boolean
)

/** Minimal, bounds-checked radiotap reader. Fields are consumed in bit order, so parsing stops at the first unknown field. */
internal object Radiotap {
    private const val MAX_PRESENT_WORDS = 8

    fun parse(b: ByteArray): Pair<RadiotapHeader?, MalformedReason?> {
        if (b.size < 8) return null to MalformedReason.RADIOTAP_TRUNCATED
        if (b[0].toInt() != 0) return null to MalformedReason.RADIOTAP_BAD_VERSION
        val len = u16(b, 2)
        if (len < 8 || len > b.size) return null to MalformedReason.RADIOTAP_BAD_LENGTH

        // Present words chain through bit 31.
        var off = 4
        val present = ArrayList<Long>(1)
        while (true) {
            if (off + 4 > len || present.size >= MAX_PRESENT_WORDS) return null to MalformedReason.RADIOTAP_BAD_LENGTH
            val w = u32(b, off)
            present.add(w)
            off += 4
            if (w and (1L shl 31) == 0L) break
        }
        val first = present[0]
        // Namespace switches (bits 29/30) change field meaning; we only decode the default namespace.
        val knownMask = (1L shl 15) - 1

        var tsft: Long? = null
        var flags = 0
        var rate: Int? = null
        var freq: Int? = null
        var signal: Int? = null
        var noise: Int? = null

        fun align(a: Int) { off = (off + a - 1) / a * a }
        fun fits(n: Int) = off + n <= len

        var bit = 0
        while (bit < 15) {
            if (first and (1L shl bit) != 0L) {
                when (bit) {
                    0 -> { align(8); if (!fits(8)) break; tsft = u64(b, off); off += 8 }
                    1 -> { if (!fits(1)) break; flags = b[off].toInt() and 0xFF; off += 1 }
                    2 -> { if (!fits(1)) break; rate = (b[off].toInt() and 0xFF) * 500; off += 1 }
                    3 -> { align(2); if (!fits(4)) break; freq = u16(b, off); off += 4 }
                    4 -> { if (!fits(2)) break; off += 2 }
                    5 -> { if (!fits(1)) break; signal = b[off].toInt(); off += 1 }
                    6 -> { if (!fits(1)) break; noise = b[off].toInt(); off += 1 }
                    7, 8, 9, 14 -> { align(2); if (!fits(2)) break; off += 2 }
                    10, 11, 12, 13 -> { if (!fits(1)) break; off += 1 }
                }
            }
            bit++
        }
        // Unknown fields after bit 14 are never read; remember nothing about them.
        @Suppress("UNUSED_VARIABLE") val ignored = first and knownMask.inv()

        val (ch, band) = freq?.let { channelFor(it) } ?: (null to null)
        val radio = RadioInfo(freq, ch, band, signal, noise, rate, tsft)
        return RadiotapHeader(len, radio, fcsPresent = flags and 0x10 != 0) to null
    }

    fun channelFor(mhz: Int): Pair<Int?, Band?> = when {
        mhz == 2484 -> 14 to Band.GHZ_2_4
        mhz in 2412..2472 && (mhz - 2407) % 5 == 0 -> (mhz - 2407) / 5 to Band.GHZ_2_4
        mhz in 5160..5885 && mhz % 5 == 0 -> (mhz - 5000) / 5 to Band.GHZ_5
        mhz in 5955..7115 && (mhz - 5950) % 5 == 0 -> (mhz - 5950) / 5 to Band.GHZ_6
        else -> null to null
    }

    internal fun u16(b: ByteArray, o: Int) = (b[o].toInt() and 0xFF) or ((b[o + 1].toInt() and 0xFF) shl 8)
    internal fun u32(b: ByteArray, o: Int) = (u16(b, o).toLong()) or (u16(b, o + 2).toLong() shl 16)
    internal fun u64(b: ByteArray, o: Int) = u32(b, o) or (u32(b, o + 4) shl 32)
}
