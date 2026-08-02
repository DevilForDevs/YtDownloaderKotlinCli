package demuxers.models.moov.trak.mdia.minf.stbl

import demuxers.BoxHeader
import demuxers.models.moov.FullBox
import muxers.models.ChunkPlan
import java.io.RandomAccessFile

class Stsc(
    header: BoxHeader,
    val doLogging: Boolean
) : FullBox(header) {


    var entryCount: Long = 0
        private set

    override fun parse(reader: RandomAccessFile) {

        parseFullBoxHeader(reader)

        entryCount = reader.readUnsignedInt()

        repeat(entryCount.toInt()) { index ->

            val firstChunk = reader.readUnsignedInt()
            val samplesPerChunk = reader.readUnsignedInt()
            val sampleDescriptionIndex = reader.readUnsignedInt()

            if (doLogging){
                println(
                    "stsc[$index] firstChunk=$firstChunk, " +
                            "samplesPerChunk=$samplesPerChunk, " +
                            "sampleDescriptionIndex=$sampleDescriptionIndex"
                )
            }

        }
    }



    fun writeStscBox(output: RandomAccessFile, plan: ChunkPlan) {
        val entryCount = if (plan.lastChunkSamples > 0) 3 else 2
        val boxSize = 8 + 4 + 4 + entryCount * 12

        output.writeInt(boxSize)
        output.writeBytes("stsc")
        output.writeInt(0) // version + flags
        output.writeInt(entryCount)

        // Entry 1: Chunk 1 -> firstChunkSamples (e.g. 2)
        output.writeInt(1) // first_chunk
        output.writeInt(plan.firstChunkSamples)
        output.writeInt(1) // sample_description_index

        // Entry 2: Chunk 2 onward -> middleChunkSamples (e.g. 6)
        output.writeInt(2) // first_chunk
        output.writeInt(plan.middleChunkSamples)
        output.writeInt(1)

        // Entry 3: Last chunk -> remaining samples
        if (plan.lastChunkSamples > 0) {
            output.writeInt(2 + plan.middleChunkCount) // first_chunk
            output.writeInt(plan.lastChunkSamples)
            output.writeInt(1)
        }
    }

    companion object {

        fun getBox(
            header: BoxHeader,
            reader: RandomAccessFile,
            doLogging: Boolean
        ): Stsc? {

            return try {
                Stsc(header, doLogging).apply {
                    parse(reader)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}