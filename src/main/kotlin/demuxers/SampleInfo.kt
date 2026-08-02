package demuxers

data class SampleInfo(
    val size: Long,
    val isKeyFrame: Boolean? = null,
    val ctts: Long? = null,
    val absOffset: Long? = null
)