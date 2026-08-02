package demuxers.models.moov.trak.mdia.minf

import demuxers.BoxHeader
import demuxers.models.moov.FullBox
import java.io.RandomAccessFile

class Nmhd private constructor(
    header: BoxHeader
) : FullBox(header) {

    override fun parse(reader: RandomAccessFile) {
        parseFullBoxHeader(reader)
    }

    companion object {

        fun getBox(
            header: BoxHeader,
            reader: RandomAccessFile,
            doLogging: Boolean
        ): Nmhd? {

            return try {
                Nmhd(header).apply {
                    parse(reader)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}