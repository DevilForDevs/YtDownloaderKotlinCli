package demuxers.models.moov.trak.mdia.minf.stbl

import demuxers.BoxHeader
import demuxers.models.moov.FullBox
import muxers.models.ChunkPlan
import java.io.RandomAccessFile

class Stco(
    header: BoxHeader,
    val doLogging: Boolean
) : FullBox(header) {

    var entryCount: Long = 0
    var futurOffset=0L
    var entrtyIndex=0



    override fun parse(reader: RandomAccessFile) {

        parseFullBoxHeader(reader)

        entryCount = reader.readUnsignedInt()
        if (doLogging){
            println("stco entry count:$entryCount")
        }


    }

    fun writeStcoBox(output: RandomAccessFile,plan: ChunkPlan){
        entryCount=plan.chunkCount.toLong()
        val stcoSize = 8 + 4 + 4 + (plan.chunkCount * 4)
        futurOffset = output.filePointer
        output.writeInt(stcoSize)
        output.write("stco".toByteArray(Charsets.US_ASCII))
        output.writeInt(0) // version + flags
        output.writeInt(plan.chunkCount)
        output.write(ByteArray(plan.chunkCount * 4)) // placeholder for chunk_offset entries

    }

    fun writeStcoEntry(output: RandomAccessFile,chunkOffset: Int) {
        if(entryCount < entrtyIndex){
            println("extra offset: $chunkOffset")
            return
        }
        val originalPos = output.filePointer
        // Header: size(4) + type(4) + version+flags(4) + entry_count(4) = 16 bytes
        val entriesStart = futurOffset + 16
        val entryOffset = entriesStart + entrtyIndex * 4L // each entry = 4 bytes

        output.seek(entryOffset)
        output.writeInt(chunkOffset)
        output.seek(originalPos)
        entrtyIndex++
    }


    companion object {

        fun getBox(
            header: BoxHeader,
            reader: RandomAccessFile,
            doLogging: Boolean
        ): Stco? {

            return try {
                Stco(header, doLogging).apply {
                    parse(reader)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}