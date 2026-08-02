package demuxers

data class BoxHeader(
    val type: String,
    val size: Long,
    val offset: Long,
    val payloadOffset: Long,
    val payloadSize: Long
)