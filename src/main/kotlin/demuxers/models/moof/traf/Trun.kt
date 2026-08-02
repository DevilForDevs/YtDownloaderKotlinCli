package demuxers.models.moof.traf

import demuxers.BoxHeader
import demuxers.SampleInfo
import demuxers.models.moov.FullBox
import demuxers.models.moov.trak.mdia.minf.Stbl
import demuxers.models.moov.trak.mdia.minf.stbl.Ctts
import demuxers.models.moov.trak.mdia.minf.stbl.Stsz
import demuxers.models.moov.trak.mdia.minf.stbl.Stts
import java.io.RandomAccessFile

class Trun private constructor(
    header: BoxHeader,
    private val baseDecodeTime: Long?,
    private val tfhd: Tfhd?,
    val stbl: Stbl?,
    val trakType: String
) : FullBox(header) {

    var samplesRetrivedCont=0
    var sampleCount: Int = 0
        private set
    var entriesFromOffset: Long?=null
    var nextSampleOffset: Long?=null
    var firstSampleFlags: Long? = null
        private set



    override fun parse(reader: RandomAccessFile) {
        parseFullBoxHeader(reader)
        sampleCount = reader.readUnsignedInt().toInt()
        stbl?.stsz?.sampleCount = stbl.stsz?.sampleCount?.plus(sampleCount) ?: 0

        val hasDataOffset = (flags and 0x000001) != 0
        val hasFirstSampleFlags = (flags and 0x000004) != 0
        val hasSampleDuration = (flags and 0x000100) != 0
        val hasSampleFlags = (flags and 0x000400) != 0
        val hasCompositionOffset = (flags and 0x000800) != 0

        if (hasDataOffset) {
            reader.skipBytes(4)
        }

        if (hasFirstSampleFlags) {
            firstSampleFlags = reader.readUnsignedInt()
        }

        repeat(sampleCount) { index ->

            // sample_duration
            val duration =
                if (hasSampleDuration)
                    reader.readUnsignedInt()
                else
                    tfhd?.defaultSampleDuration ?: 0L

            stbl?.stts?.addDuration(duration)

            // sample_size
            val size =
                if ((flags and 0x000200) != 0)
                    reader.readUnsignedInt()
                else
                    tfhd?.defaultSampleSize ?: 0L



            // sample_flags
            val sampleFlags =
                when {
                    hasSampleFlags ->
                        reader.readUnsignedInt()

                    index == 0 && firstSampleFlags != null ->
                        firstSampleFlags!!

                    else ->
                        tfhd?.defaultSampleFlags ?: 0L
                }

            // sample_composition_time_offset
            val compositionOffset =
                if (hasCompositionOffset) {
                    if (version == 1)
                        reader.readInt().toLong()
                    else
                        reader.readUnsignedInt()
                } else {
                    0L
                }

            if ((sampleFlags and 0x00010000L) == 0L) {
                stbl?.stss?.entryCount = stbl.stss?.entryCount?.plus(1) ?: 0
            }

            stbl?.ctts?.addCompositionOffset(compositionOffset)
        }



    }

    fun getSamplesInfo(
        reader: RandomAccessFile,
        requestedSampleCount: Int,
        handlerType: String?
    ): MutableList<SampleInfo> {

        val collectedSamples = mutableListOf<SampleInfo>()

        if (samplesRetrivedCont >= sampleCount)
            return collectedSamples

        val samplesToRead = minOf(
            requestedSampleCount,
            (sampleCount - samplesRetrivedCont)
        )

        reader.seek(header.payloadOffset)
        samplesRetrivedCont += samplesToRead

        parseFullBoxHeader(reader)

        // skip sample count
        reader.skipBytes(4)

        if ((flags and 0x000001) != 0) {
            // skip data offset
            reader.skipBytes(4)
        }

        if ((flags and 0x000004) != 0) {
            firstSampleFlags = reader.readUnsignedInt()
        }

        entriesFromOffset?.let { reader.seek(it) }

        repeat(samplesToRead) { index ->

            val size =
                if ((flags and 0x000200) != 0)
                    reader.readUnsignedInt()
                else
                    tfhd?.defaultSampleSize ?: 0L

            if (handlerType == "vide") {

                val sampleFlags =
                    if ((flags and 0x000400) != 0) {
                        reader.readUnsignedInt()
                    } else if (index == 0 && firstSampleFlags != null) {
                        firstSampleFlags!!
                    } else {
                        tfhd?.defaultSampleFlags ?: 0L
                    }

                val compositionOffset =
                    if ((flags and 0x000800) != 0) {
                        if (version == 1)
                            reader.readInt().toLong()
                        else
                            reader.readUnsignedInt()
                    } else {
                        0L
                    }

                val isKeyFrame = (sampleFlags and 0x00010000L) == 0L

                collectedSamples.add(
                    SampleInfo(
                        size = size,
                        isKeyFrame = isKeyFrame,
                        ctts = compositionOffset,
                        absOffset = nextSampleOffset
                    )
                )

            } else {

                if ((flags and 0x000400) != 0)
                    reader.skipBytes(4)

                if ((flags and 0x000800) != 0)
                    reader.skipBytes(4)

                collectedSamples.add(
                    SampleInfo(
                        size = size,
                        absOffset = nextSampleOffset
                    )
                )
            }

            nextSampleOffset = (nextSampleOffset ?: 0L) + size
            entriesFromOffset = reader.filePointer
        }

        return collectedSamples
    }

    fun setNextSampleOffset(
        reader: RandomAccessFile,
        moofOffset: Long,
    ){
        reader.seek(header.payloadOffset)

        parseFullBoxHeader(reader)
        sampleCount = reader.readUnsignedInt().toInt()

        if ((flags and 0x000001) != 0) {
            nextSampleOffset=moofOffset+ reader.readInt()
        }
    }


    companion object {

        fun getBox(
            header: BoxHeader,
            reader: RandomAccessFile,
            baseDecodeTime: Long?,
            tfhd: Tfhd?,
             stbl: Stbl?,
            trakType: String
        ): Trun? {

            return try {
                Trun(
                    header,
                    baseDecodeTime,
                    tfhd,
                    stbl,
                    trakType
                ).apply {
                    parse(reader)
                }

            } catch (e: Exception) {
                null
            }
        }



        fun getBox(
            header: BoxHeader,
            reader: RandomAccessFile,
            moofOffset: Long,
            tfhd: Tfhd?,
        ): Trun? {

            return try {
                Trun(
                    header,
                    null,
                    tfhd,
                    null,""
                ).apply {
                    setNextSampleOffset(reader,moofOffset)
                }
            } catch (e: Exception) {
                null
            }

        }
    }
}