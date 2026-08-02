package demuxers.models.moof.traf

import demuxers.BoxHeader
import demuxers.models.moov.FullBox
import java.io.RandomAccessFile

class Tfdt private constructor(
    header: BoxHeader
) : FullBox(header) {

    var baseMediaDecodeTime: Long = 0
        private set


    override fun parse(reader: RandomAccessFile) {

        parseFullBoxHeader(reader)

        baseMediaDecodeTime =
            if (version == 1) {
                reader.readLong()
            } else {
                reader.readUnsignedInt()
            }
    }


    companion object {

        fun getBox(
            header: BoxHeader,
            reader: RandomAccessFile
        ): Tfdt? {

            return try {
                Tfdt(header).apply {
                    parse(reader)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}