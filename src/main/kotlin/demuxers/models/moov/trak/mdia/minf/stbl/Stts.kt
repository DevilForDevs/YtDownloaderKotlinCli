package demuxers.models.moov.trak.mdia.minf.stbl

import demuxers.BoxHeader
import demuxers.models.moov.FullBox
import demuxers.models.moov.trak.mdia.minf.stbl.stts.SttsEntry
import java.io.RandomAccessFile

class Stts(
    header: BoxHeader,
    val doLogging: Boolean
) : FullBox(header) {

    private val entries = mutableListOf<SttsEntry>()
    var entryCount: Long = 0


    override fun parse(reader: RandomAccessFile) {

        parseFullBoxHeader(reader)

        entryCount = reader.readUnsignedInt()

        repeat(entryCount.toInt()) {

            val sampleCount = reader.readUnsignedInt()
            val sampleDelta = reader.readUnsignedInt()

            entries += SttsEntry(sampleCount, sampleDelta)
        }
    }

    fun writeSttsBox(output: RandomAccessFile) {

        val payloadSize =
            4L + // version + flags
                    4L + // entry count
                    entries.size * 8L // each entry = sample_count + sample_delta

        val boxSize = payloadSize + 8L // size + type

        output.writeInt(boxSize.toInt())
        output.writeBytes("stts")

        // FullBox header
        output.writeByte(0) // version
        output.writeByte(0) // flags
        output.writeByte(0)
        output.writeByte(0)

        // entry_count
        output.writeInt(entries.size)

        // entries
        entries.forEach { entry ->
            output.writeInt(entry.sampleCount.toInt())
            output.writeInt(entry.sampleDelta.toInt())
        }
    }
    fun addDuration(duration: Long) {
        if (entries.isEmpty()) {
            entries += SttsEntry(1, duration)
            return
        }

        val last = entries.last()

        if (last.sampleDelta == duration) {
            last.sampleCount++
        } else {
            entries += SttsEntry(1, duration)
        }
    }

    companion object {

        fun getBox(
            header: BoxHeader,
            reader: RandomAccessFile,
            doLogging: Boolean
        ): Stts? {

            return try {
                Stts(header,doLogging).apply {
                    parse(reader)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}