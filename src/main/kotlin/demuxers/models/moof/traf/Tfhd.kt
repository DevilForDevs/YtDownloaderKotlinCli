package demuxers.models.moof.traf

import demuxers.BoxHeader
import demuxers.models.moov.FullBox
import java.io.RandomAccessFile

class Tfhd private constructor(
    header: BoxHeader
) : FullBox(header) {

    var trackId: Long = 0
        private set

    var baseDataOffset: Long? = null
        private set

    var sampleDescriptionIndex: Long? = null
        private set

    var defaultSampleDuration: Long? = null
        private set

    var defaultSampleSize: Long? = null
        private set

    var defaultSampleFlags: Long? = null
        private set


    override fun parse(reader: RandomAccessFile) {

        parseFullBoxHeader(reader)

        trackId = reader.readUnsignedInt()


        if ((flags and 0x000001) != 0) {
            baseDataOffset = reader.readLong()
        }


        if ((flags and 0x000002) != 0) {
            sampleDescriptionIndex = reader.readUnsignedInt()
        }


        if ((flags and 0x000008) != 0) {
            defaultSampleDuration = reader.readUnsignedInt()
        }


        if ((flags and 0x000010) != 0) {
            defaultSampleSize = reader.readUnsignedInt()
        }


        if ((flags and 0x000020) != 0) {
            defaultSampleFlags = reader.readUnsignedInt()
        }
    }


    companion object {

        fun getBox(
            header: BoxHeader,
            reader: RandomAccessFile
        ): Tfhd? {

            return try {
                Tfhd(header).apply {
                    parse(reader)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}