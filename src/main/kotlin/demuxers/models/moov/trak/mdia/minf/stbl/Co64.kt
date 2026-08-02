package demuxers.models.moov.trak.mdia.minf.stbl

import demuxers.BoxHeader
import demuxers.models.moov.FullBox
import java.io.RandomAccessFile

class Co64 private constructor(
    header: BoxHeader
) : FullBox(header) {

    var entryCount: Long = 0
        private set

    val entries = mutableListOf<Long>()

    override fun parse(reader: RandomAccessFile) {

        parseFullBoxHeader(reader)

        entryCount = reader.readUnsignedInt()

        repeat(entryCount.toInt()) {

            val chunkOffset = reader.readLong()

            /* entries.add(chunkOffset) */
        }
    }

    companion object {

        fun getBox(
            header: BoxHeader,
            reader: RandomAccessFile,
            doLogging: Boolean
        ): Co64? {

            return try {
                Co64(header).apply {
                    parse(reader)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}