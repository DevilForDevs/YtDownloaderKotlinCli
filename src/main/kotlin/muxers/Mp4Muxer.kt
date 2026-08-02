package muxers

import demuxers.fmp4.FMp4Parser
import muxers.models.Ftyp
import java.io.RandomAccessFile

class Mp4Muxer(
    private val output: RandomAccessFile,
    private val sources: MutableList<FMp4Parser>,
    private val onSamplesWritten: ((String) -> Unit)? = null

){

    fun mux(){
        Ftyp(
            listOf(
                "isom",
                "iso2",
                "mp41"
            )
        ).write(output)
        writeMoov()
        writeMdat()
    }

    private fun writeMoov() {
        val moovStart = output.filePointer
        output.writeInt(0)
        output.write("moov".toByteArray())


        val videoSource = sources.first { it.moov?.traks?.first()?.mdia?.hdlr?.handlerType == "soun" }
        val videoTimeScale = videoSource.moov?.mvhd?.timescale!!
        // ------------ Convert all durations to VIDEO timescale ------------
        val maxDuration = sources.maxOf { source ->
            val srcMvhd = source.moov?.mvhd!!
            (srcMvhd.duration.toDouble() * videoTimeScale / srcMvhd.timescale).toLong()
        }

        val mvhdBox = writeMvhd(
            videoSource.moov?.mvhd!!,
            videoTimeScale.toInt(),
            maxDuration.toInt()
        )

        output.write(mvhdBox)

        for (source in sources){
            source.moov?.traks?.first()?.writeFutureTrak(output,source.reader)
        }

        val moovEnd = output.filePointer
        val moovSize = (moovEnd - moovStart).toInt()
        output.seek(moovStart)
        output.writeInt(moovSize)
        output.seek(moovEnd)
    }

    fun writeMdat() {
        val mdatStart = output.filePointer

        output.writeInt(0)
        output.write("mdat".toByteArray())

        val totalSamples = sources.sumOf {
            it.moov?.traks?.firstOrNull()?.mdia?.minf?.stbl?.stsz?.sampleCount ?: 0
        }

        var sampleRead = 0L
        var nextProgressUpdate = 2000L

        while (sampleRead < totalSamples) {
            var wroteAny = false

            for (source in sources) {
                val written = source.moov?.traks?.firstOrNull()
                    ?.writeSamplesTo(output, source.reader) ?: 0

                if (written > 0) {
                    sampleRead += written
                    wroteAny = true

                    if (sampleRead >= nextProgressUpdate || sampleRead == totalSamples.toLong()) {
                        val percent = (sampleRead * 100 / totalSamples)
                        onSamplesWritten?.invoke(
                            "Merging-$sampleRead/$totalSamples $percent%"
                        )
                        nextProgressUpdate += 2000
                    }
                }
            }

            if (!wroteAny) break
        }

        val mdatEnd = output.filePointer
        val mdatSize = (mdatEnd - mdatStart).toInt()

        output.seek(mdatStart)
        output.writeInt(mdatSize)
        output.seek(mdatEnd)
    }


}