package demuxers.models.moov.trak.mdia.minf.stbl.stts

data class SttsEntry(
    var sampleCount: Long,
    val sampleDelta: Long
)