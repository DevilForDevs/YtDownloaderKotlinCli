package demuxers.models.moov.trak.mdia.minf

import demuxers.BoxHeader
import demuxers.models.moov.FullBox
import java.io.RandomAccessFile

class Smhd private constructor(
    header: BoxHeader
) : FullBox(header) {

    var balance: Short = 0
        private set

    override fun parse(reader: RandomAccessFile) {

        parseFullBoxHeader(reader)

        balance = reader.readShort()

        // reserved
        reader.readShort()
    }

    companion object {

        fun getBox(
            header: BoxHeader,
            reader: RandomAccessFile,
            doLogging: Boolean
        ): Smhd? {

            return try {
                Smhd(header).apply {
                    parse(reader)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}