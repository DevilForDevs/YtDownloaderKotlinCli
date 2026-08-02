package demuxers.models.moof

import demuxers.BoxHeader
import demuxers.models.moov.FullBox
import java.io.RandomAccessFile

class Mfhd private constructor(
    header: BoxHeader
) : FullBox(header) {

    var sequenceNumber: Long = 0
        private set


    override fun parse(reader: RandomAccessFile) {

        parseFullBoxHeader(reader)

        sequenceNumber = reader.readUnsignedInt()
    }


    companion object {

        fun getBox(
            header: BoxHeader,
            reader: RandomAccessFile
        ): Mfhd? {

            return try {
                Mfhd(header).apply {
                    parse(reader)
                }
            } catch (e: Exception) {
                null
            }
        }
    }


}