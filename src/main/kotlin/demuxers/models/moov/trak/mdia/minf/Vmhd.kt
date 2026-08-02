package demuxers.models.moov.trak.mdia.minf

import demuxers.BoxHeader
import demuxers.models.moov.FullBox
import java.io.RandomAccessFile

class Vmhd private constructor(
    header: BoxHeader
) : FullBox(header) {

    var graphicsMode: Int = 0
        private set

    var opColorRed: Int = 0
        private set

    var opColorGreen: Int = 0
        private set

    var opColorBlue: Int = 0
        private set

    override fun parse(reader: RandomAccessFile) {

        parseFullBoxHeader(reader)

        graphicsMode = reader.readUnsignedShort()
        opColorRed = reader.readUnsignedShort()
        opColorGreen = reader.readUnsignedShort()
        opColorBlue = reader.readUnsignedShort()
    }

    companion object {

        fun getBox(
            header: BoxHeader,
            reader: RandomAccessFile,
            doLogging: Boolean
        ): Vmhd? {

            return try {
                Vmhd(header).apply {
                    parse(reader)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}