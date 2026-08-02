package demuxers.models.moov.trak.mdia.minf.stbl

import demuxers.BoxHeader
import demuxers.models.moov.FullBox
import java.io.RandomAccessFile

class Stsz(
    header: BoxHeader,
    val doLogging: Boolean
) : FullBox(header) {

    var sampleSize: Long = 0
        private set

    var sampleCount: Long = 0
    var futureOffset=0L
    var entryIndex=0


    override fun parse(reader: RandomAccessFile) {

        parseFullBoxHeader(reader)

        sampleSize = reader.readUnsignedInt()

        sampleCount = reader.readUnsignedInt()
        if (doLogging){
            println("Stsz Total Sample Count: $sampleCount")
        }
        if (sampleSize == 0L) {
            repeat(minOf(10, sampleCount.toInt())) { index ->
                val size = reader.readUnsignedInt()
                if (doLogging) {
                    println("stsz[$index] = $size")
                }
            }
        }

    }

    fun writeStszBox(output: RandomAccessFile) {
        val stszSize = 8 + 4 + 4 + 4 + (sampleCount * 4)
        futureOffset = output.filePointer
        output.writeInt(stszSize.toInt())
        output.write("stsz".toByteArray(Charsets.US_ASCII))
        output.writeInt(0) // version+flags
        output.writeInt(0) // sample_size (0 => variable)
        output.writeInt(sampleCount.toInt())
        output.write(ByteArray(sampleCount.toInt() * 4))
    }

    fun writeStszEntry(
        output: RandomAccessFile,
        sampleSize: Int
    ) {
        if (entryIndex>=sampleCount){
            println("extra entry index$entryIndex")
            return
        }
        val currentPos = output.filePointer

        // stsz payload layout:
        // version+flags (4)
        // sample_size   (4)
        // sample_count  (4)
        // entries...    (4 bytes each)
        output.seek(futureOffset + 8 + 4 + 4 + 4L + entryIndex * 4L)

        output.writeInt(sampleSize)
        entryIndex++

        output.seek(currentPos)
    }

    companion object {

        fun getBox(
            header: BoxHeader,
            reader: RandomAccessFile,
            doLogging: Boolean
        ): Stsz? {

            return try {
                Stsz(header,doLogging).apply {
                    parse(reader)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}