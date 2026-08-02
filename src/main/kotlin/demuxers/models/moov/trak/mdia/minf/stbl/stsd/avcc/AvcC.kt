package demuxers.models.moov.trak.mdia.minf.stbl.stsd.avcc

import demuxers.Box
import demuxers.BoxHeader
import java.io.RandomAccessFile

class AvcC private constructor(
    header: BoxHeader
) : Box(header) {

    var configurationVersion: Int = 0
        private set

    var profile: Int = 0
        private set

    var compatibility: Int = 0
        private set

    var level: Int = 0
        private set

    var nalLengthSize: Int = 0
        private set

    val sps = mutableListOf<ByteArray>()

    val pps = mutableListOf<ByteArray>()


    override fun parse(reader: RandomAccessFile) {

        configurationVersion = reader.readUnsignedByte()

        profile = reader.readUnsignedByte()

        compatibility = reader.readUnsignedByte()

        level = reader.readUnsignedByte()


        val lengthField =
            reader.readUnsignedByte()

        nalLengthSize =
            (lengthField and 0x03) + 1


        val spsCount =
            reader.readUnsignedByte() and 0x1F


        repeat(spsCount) {

            val spsLength =
                reader.readUnsignedShort()

            val data = ByteArray(spsLength)

            reader.readFully(data)

            sps.add(data)
        }


        val ppsCount =
            reader.readUnsignedByte()


        repeat(ppsCount) {

            val ppsLength =
                reader.readUnsignedShort()

            val data = ByteArray(ppsLength)

            reader.readFully(data)

            pps.add(data)
        }
    }


    companion object {

        fun getBox(
            header: BoxHeader,
            reader: RandomAccessFile
        ): AvcC? {

            return try {
                AvcC(header).apply {
                    parse(reader)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}