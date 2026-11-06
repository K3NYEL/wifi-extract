package com.wifiextract.app.core.dot11

/** Aggregated view of what the API has seen. Immutable snapshot, safe to hand to the UI. */
data class Dot11Stats(
    val framesAccepted: Long,
    val framesRejected: Long,
    val retryFrames: Long,
    val byType: Map<FrameType, Long>,
    val bySubtype: Map<String, Long>,
    val byChannel: Map<Int, Long>,
    val networks: List<ObservedNetwork>
)

data class ObservedNetwork(
    val bssid: MacAddress,
    val ssid: String?,
    val hidden: Boolean,
    val channel: Int?,
    val security: SecurityType,
    val lastSignalDbm: Int?,
    val beacons: Long
)

/** Query over the bounded frame buffer. Null fields do not filter. */
data class FrameFilter(
    val type: FrameType? = null,
    val subtypeName: String? = null,
    val channel: Int? = null,
    val bssid: MacAddress? = null,
    val minSignalDbm: Int? = null
) {
    fun matches(f: Dot11Frame): Boolean =
        (type == null || f.type == type) &&
            (subtypeName == null || f.subtypeName == subtypeName) &&
            (channel == null || f.effectiveChannel == channel) &&
            (bssid == null || f.bssid == bssid) &&
            (minSignalDbm == null || (f.radio?.signalDbm ?: Int.MIN_VALUE) >= minSignalDbm)
}

/**
 * Entry point of the 802.11 frame API: feed it raw captures from a genuine provider,
 * read back parsed metadata, statistics and observed networks.
 *
 * Passive only (no transmit/inject path), bounded memory, thread-safe, no payload retention.
 * It never fabricates frames: with no input, every query is empty.
 */
class Dot11FrameApi(
    private val capacity: Int = 500,
    private val maxNetworks: Int = 256,
    private val maxChannels: Int = 256
) {
    init { require(capacity in 1..10_000 && maxNetworks in 1..4096) { "invalid limits" } }

    private val lock = Any()
    private val buffer = ArrayDeque<Dot11Frame>()
    private val networks = LinkedHashMap<MacAddress, ObservedNetwork>()
    private val byType = HashMap<FrameType, Long>()
    private val bySubtype = HashMap<String, Long>()
    private val byChannel = HashMap<Int, Long>()
    private var accepted = 0L
    private var rejected = 0L
    private var retries = 0L
    private val listeners = java.util.concurrent.CopyOnWriteArrayList<(Dot11Frame) -> Unit>()

    /** Ingests a capture that starts with a radiotap header. Returns the parse result. */
    fun ingestRadiotap(bytes: ByteArray): Dot11ParseResult = record(Dot11Parser.parseRadiotap(bytes))

    /** Ingests a bare 802.11 frame, optionally with reception metadata supplied by the provider. */
    fun ingest(bytes: ByteArray, radio: RadioInfo? = null): Dot11ParseResult = record(Dot11Parser.parse(bytes, radio))

    /** Registers a callback for each accepted frame. Returns a function that unregisters it. */
    fun addListener(listener: (Dot11Frame) -> Unit): () -> Unit {
        listeners.add(listener)
        return { listeners.remove(listener) }
    }

    fun frames(filter: FrameFilter = FrameFilter(), limit: Int = 100): List<Dot11Frame> = synchronized(lock) {
        buffer.asReversed().asSequence().filter(filter::matches).take(limit.coerceIn(0, capacity)).toList()
    }

    fun stats(): Dot11Stats = synchronized(lock) {
        Dot11Stats(accepted, rejected, retries, byType.toMap(), bySubtype.toMap(), byChannel.toMap(), networks.values.toList())
    }

    fun clear() = synchronized(lock) {
        buffer.clear(); networks.clear(); byType.clear(); bySubtype.clear(); byChannel.clear()
        accepted = 0; rejected = 0; retries = 0
    }

    private fun record(result: Dot11ParseResult): Dot11ParseResult {
        when (result) {
            is Dot11ParseResult.Malformed -> synchronized(lock) { rejected++ }
            is Dot11ParseResult.Parsed -> {
                val f = result.frame
                synchronized(lock) {
                    accepted++
                    if (f.flags.retry) retries++
                    if (buffer.size == capacity) buffer.removeFirst()
                    buffer.addLast(f)
                    byType.merge(f.type, 1L, Long::plus)
                    bySubtype.merge(f.subtypeName, 1L, Long::plus)
                    f.effectiveChannel?.let { if (byChannel.size < maxChannels || it in byChannel) byChannel.merge(it, 1L, Long::plus) }
                    trackNetwork(f)
                }
                listeners.forEach { runCatching { it(f) } }
            }
        }
        return result
    }

    private fun trackNetwork(f: Dot11Frame) {
        if (f.type != FrameType.MANAGEMENT || (f.subtypeName != "Beacon" && f.subtypeName != "Probe Response")) return
        val id = f.bssid ?: return
        val prev = networks[id]
        if (prev == null && networks.size >= maxNetworks) return
        networks[id] = ObservedNetwork(
            bssid = id,
            ssid = f.ssid ?: prev?.ssid,
            hidden = f.ssidHidden && prev?.ssid == null,
            channel = f.effectiveChannel ?: prev?.channel,
            security = f.security,
            lastSignalDbm = f.radio?.signalDbm ?: prev?.lastSignalDbm,
            beacons = (prev?.beacons ?: 0L) + 1
        )
    }
}
