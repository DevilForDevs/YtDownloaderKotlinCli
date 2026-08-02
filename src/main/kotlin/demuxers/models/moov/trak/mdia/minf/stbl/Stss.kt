package demuxers.models.moov.trak.mdia.minf.stbl

import demuxers.BoxHeader
import demuxers.models.moov.FullBox
import java.io.RandomAccessFile

class Stss(
    header: BoxHeader
) : FullBox(header) {

    var entryCount: Long = 0
    var futureOffset=0L
    var entryIndex=0


    override fun parse(reader: RandomAccessFile) {

        parseFullBoxHeader(reader)

        entryCount = reader.readUnsignedInt()
        repeat(entryCount.toInt()) {

            val sampleNumber = reader.readUnsignedInt()

            /* entries.add(sampleNumber) */
        }
    }

    fun writeStssBox(output: RandomAccessFile){
        val stssSize = 8 + 4 + 4 + (entryCount * 4)
        futureOffset = output.filePointer
        output.writeInt(stssSize.toInt())
        output.write("stss".toByteArray(Charsets.US_ASCII))
        output.writeInt(0) // version + flags
        output.writeInt(entryCount.toInt())
        output.write(ByteArray(entryCount.toInt() * 4)) // placeholder for sample_number entries

    }

    fun writeStssEntry(
        output: RandomAccessFile,
        sampleNumber: Int
    ) {
        if (entryIndex < entryCount){
            return
        }
        val currentPos = output.filePointer

        // Skip:
        // 8 bytes  -> box header
        // 4 bytes  -> version + flags
        // 4 bytes  -> entry_count
        output.seek(futureOffset + 8 + 4 + 4 + entryIndex * 4L)

        output.writeInt(sampleNumber)
        entryIndex++

        output.seek(currentPos)
    }

    companion object {

        fun getBox(
            header: BoxHeader,
            reader: RandomAccessFile,
            doLogging: Boolean
        ): Stss? {

            return try {
                Stss(header).apply {
                    parse(reader)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}