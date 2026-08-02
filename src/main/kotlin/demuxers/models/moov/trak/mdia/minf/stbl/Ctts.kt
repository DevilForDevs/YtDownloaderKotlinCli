package demuxers.models.moov.trak.mdia.minf.stbl

import demuxers.BoxHeader
import demuxers.SampleInfo
import demuxers.models.moov.FullBox
import java.io.RandomAccessFile

class Ctts(
    header: BoxHeader
) : FullBox(header) {

    private var previousSampleCtts: Int? = null
    private var previousSamplesCountWithAboveCtts = 0

    private var futureOffset = 0L

    /** Number of CTTS entries that will be written. */
    var entryCount: Long = 0

    /** True if any composition offset is negative. */
    var hasNegativeCtts = false

    /** Number of entries actually written. */
     var entryIndex = 0


    override fun parse(reader: RandomAccessFile) {

        parseFullBoxHeader(reader)

        entryCount = reader.readUnsignedInt()

        println("ctts version:$version")
        println("ctts entry count:$entryCount")

        repeat(entryCount.toInt()) { index ->

            val sampleCount = reader.readUnsignedInt()

            val sampleOffset = if (version == 1) {
                reader.readInt().toLong()
            } else {
                reader.readUnsignedInt()
            }

            /*println(
                "ctts[$index] sampleCount=$sampleCount offset=$sampleOffset"
            )*/
        }
    }

    fun writeCttsBox(output: RandomAccessFile) {
        futureOffset = output.filePointer
        val size = 8 + 4 + 4 + (entryCount * 8)

        output.writeInt(size.toInt())
        output.write("ctts".toByteArray(Charsets.US_ASCII))

        // FullBox: version (1 byte) + flags (3 bytes)
        val version = if (hasNegativeCtts) 1 else 0
        output.writeInt(version shl 24)

        output.writeInt(entryCount.toInt())

        // Placeholder entries
        output.write(ByteArray(entryCount.toInt() * 8))

        previousSampleCtts = null
        previousSamplesCountWithAboveCtts = 0
        entryIndex=0
    }

    fun writePreviousCttsEntry(
        output: RandomAccessFile,
        ctts: Int?
    ) {
        if (ctts==null){
            writeEntry(output,previousSamplesCountWithAboveCtts, previousSampleCtts ?: 0)
            return
        }
        if (previousSampleCtts==null){
            previousSampleCtts= ctts
            previousSamplesCountWithAboveCtts=1
            return
        }
        if (previousSampleCtts ==ctts){
            previousSamplesCountWithAboveCtts++
            return
        }
        writeEntry(output,previousSamplesCountWithAboveCtts, previousSampleCtts ?: 0)
        previousSampleCtts= ctts
        previousSamplesCountWithAboveCtts=1
    }


    private fun writeEntry(
        output: RandomAccessFile,
        sampleCount: Int,
        sampleOffset: Int
    ) {
        val currentPos = output.filePointer

        val entriesStart = futureOffset + 16
        val entryOffset = entriesStart + entryIndex * 8L

        output.seek(entryOffset)

        output.writeInt(sampleCount)
        output.writeInt(sampleOffset)

        output.seek(currentPos)
        entryIndex++
    }

    fun addCompositionOffset(offset: Long) {

        if (offset < 0) {
            hasNegativeCtts = true
        }

        val currentOffset = offset.toInt()

        if (previousSampleCtts == null) {
            previousSampleCtts = currentOffset
            previousSamplesCountWithAboveCtts = 1
            entryCount = 1
            return
        }

        if (previousSampleCtts == currentOffset) {
            previousSamplesCountWithAboveCtts++
            return
        }
        previousSampleCtts = currentOffset
        previousSamplesCountWithAboveCtts = 1
        entryCount++
    }

    companion object {

        fun getBox(
            header: BoxHeader,
            reader: RandomAccessFile,
            doLogging: Boolean
        ): Ctts? {

            return try {
                Ctts(header).apply {
                    parse(reader)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}