package demuxers.models.moov.trak.mdia.minf.stbl.stsd.av01

import demuxers.Box
import demuxers.BoxHeader
import java.io.File
import java.io.RandomAccessFile

class Av1C private constructor(
    header: BoxHeader
) : Box(header) {

    var marker: Int = 0
        private set

    var version: Int = 0
        private set

    var seqProfile: Int = 0
        private set

    var seqLevelIdx0: Int = 0
        private set

    var seqTier0: Int = 0
        private set

    var highBitDepth: Boolean = false
        private set

    var twelveBit: Boolean = false
        private set

    var monochrome: Boolean = false
        private set

    var chromaSubsamplingX: Boolean = false
        private set

    var chromaSubsamplingY: Boolean = false
        private set

    var chromaSamplePosition: Int = 0
        private set


    override fun parse(reader: RandomAccessFile) {

        val byte0 = reader.readUnsignedByte()

        marker = (byte0 shr 7) and 0x01
        version = byte0 and 0x7F


        val byte1 = reader.readUnsignedByte()

        seqProfile = (byte1 shr 5) and 0x07

        seqLevelIdx0 = byte1 and 0x1F


        val byte2 = reader.readUnsignedByte()

        seqTier0 = (byte2 shr 7) and 0x01

        highBitDepth =
            ((byte2 shr 6) and 0x01) == 1

        twelveBit =
            ((byte2 shr 5) and 0x01) == 1

        monochrome =
            ((byte2 shr 4) and 0x01) == 1

        chromaSubsamplingX =
            ((byte2 shr 3) and 0x01) == 1

        chromaSubsamplingY =
            ((byte2 shr 2) and 0x01) == 1

        chromaSamplePosition =
            byte2 and 0x03


        // remaining bytes are reserved in av1C
        while (reader.filePointer < header.offset + header.size) {
            reader.readByte()
        }
    }


    companion object {

        fun getBox(
            header: BoxHeader,
            reader: RandomAccessFile
        ): Av1C? {

            return try {
                Av1C(header).apply {
                    parse(reader)
                }
            } catch (e: Exception) {
                null
            }
        }
    }




}