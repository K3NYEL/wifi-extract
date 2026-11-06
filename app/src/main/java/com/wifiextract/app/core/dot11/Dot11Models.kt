package com.wifiextract.app.core.dot11

enum class FrameType { MANAGEMENT, CONTROL, DATA, EXTENSION }

enum class Band { GHZ_2_4, GHZ_5, GHZ_6 }

enum class SecurityType { UNKNOWN, OPEN, WEP, WPA, WPA2, WPA2_WPA3, WPA3 }

data class FrameFlags(
    val toDs: Boolean,
    val fromDs: Boolean,
    val moreFragments: Boolean,
    val retry: Boolean,
    val powerManagement: Boolean,
    val moreData: Boolean,
    val protectedFrame: Boolean,
    val order: Boolean
)

/** Radiotap-derived reception metadata. Every field is optional because drivers differ. */
data class RadioInfo(
    val frequencyMhz: Int?,
    val channel: Int?,
    val band: Band?,
    val signalDbm: Int?,
    val noiseDbm: Int?,
    val rateKbps: Int?,
    val tsft: Long?
)

/**
 * Metadata of one received 802.11 frame. Payload bytes are deliberately NOT retained:
 * only header fields and, for beacons/probes, SSID/channel/security.
 */
data class Dot11Frame(
    val type: FrameType,
    val subtype: Int,
    val subtypeName: String,
    val flags: FrameFlags,
    val receiver: MacAddress?,
    val transmitter: MacAddress?,
    val bssid: MacAddress?,
    val sequenceNumber: Int?,
    val ssid: String?,
    val ssidHidden: Boolean,
    val advertisedChannel: Int?,
    val security: SecurityType,
    val radio: RadioInfo?,
    val capturedLength: Int
) {
    /** Channel from radiotap when available, otherwise the one advertised in the DS Parameter Set. */
    val effectiveChannel: Int? get() = radio?.channel ?: advertisedChannel
}

enum class MalformedReason {
    EMPTY, RADIOTAP_TRUNCATED, RADIOTAP_BAD_VERSION, RADIOTAP_BAD_LENGTH,
    FRAME_TRUNCATED, UNSUPPORTED_PROTOCOL_VERSION, FRAME_TOO_LARGE
}

sealed interface Dot11ParseResult {
    data class Parsed(val frame: Dot11Frame) : Dot11ParseResult
    data class Malformed(val reason: MalformedReason) : Dot11ParseResult
}
