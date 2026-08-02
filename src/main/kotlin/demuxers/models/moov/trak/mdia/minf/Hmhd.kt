package demuxers.models.moov.trak.mdia.minf

import demuxers.BoxHeader
import demuxers.models.moov.FullBox
import java.io.RandomAccessFile

class Hmhd private constructor(
    header: BoxHeader
) : FullBox(header) {

    var maxPduSize: Int = 0
        private set

    var avgPduSize: Int = 0
        private set

    var maxBitrate: Long = 0
        private set

    var avgBitrate: Long = 0
        private set

    override fun parse(reader: RandomAccessFile) {

        parseFullBoxHeader(reader)

        maxPduSize = reader.readUnsignedShort()
        avgPduSize = reader.readUnsignedShort()
        maxBitrate = reader.readUnsignedInt()
        avgBitrate = reader.readUnsignedInt()

        // reserved
        reader.readUnsignedInt()
    }

    companion object {

        fun getBox(
            header: BoxHeader,
            reader: RandomAccessFile,
            doLogging: Boolean
        ): Hmhd? {

            return try {
                Hmhd(header).apply {
                    parse(reader)
                }
            } catch (e: Exception) {
                null
            }
        }
    }

}