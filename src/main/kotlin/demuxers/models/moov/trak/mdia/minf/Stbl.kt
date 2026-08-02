package demuxers.models.moov.trak.mdia.minf

import demuxers.Box
import demuxers.BoxHeader
import demuxers.models.Moof
import demuxers.models.moov.trak.mdia.minf.stbl.Co64
import demuxers.models.moov.trak.mdia.minf.stbl.Ctts
import demuxers.models.moov.trak.mdia.minf.stbl.Stco
import demuxers.models.moov.trak.mdia.minf.stbl.Stsc
import demuxers.models.moov.trak.mdia.minf.stbl.Stsd
import demuxers.models.moov.trak.mdia.minf.stbl.Stss
import demuxers.models.moov.trak.mdia.minf.stbl.Stsz
import demuxers.models.moov.trak.mdia.minf.stbl.Stts
import demuxers.readBoxHeader
import muxers.models.ChunkPlan
import java.io.RandomAccessFile

class Stbl private constructor(
    header: BoxHeader,
    val doLogging: Boolean
) : Box(header) {



    var stsd: Stsd?=null
    /*initialized with invalid headers to store info from fragments*/
    var stts: Stts?=Stts(header,doLogging)
    var ctts: Ctts?= Ctts(header)
    var stsc: Stsc?= Stsc(header,doLogging)
    var stsz: Stsz?=Stsz(header,doLogging)
    var stco: Stco?=null
    var co64: Co64?=null
    var stss: Stss?=Stss(header)
    override fun parse(reader: RandomAccessFile) {

        reader.seek(header.payloadOffset)

        while (reader.filePointer < header.offset + header.size) {
            val childHeader = readBoxHeader(reader) ?: break
            if (doLogging){
                println("Stbl child boxes found ${childHeader.type}")
            }
            when (childHeader.type) {
                "stsd"->{
                    stsd= Stsd.getBox(childHeader,reader,doLogging)
                }
                "stts"->{
                    stts= Stts.getBox(childHeader,reader,doLogging)
                }
                "ctts"->{
                    ctts= Ctts.getBox(childHeader,reader,doLogging)
                }
                "stsc"->{
                    stsc= Stsc.getBox(childHeader,reader,doLogging)
                }
                "stsz"->{
                    stsz= Stsz.getBox(childHeader,reader,doLogging)
                }
                "stco"->{
                    stco= Stco.getBox(childHeader,reader,doLogging)
                }
                "co64"->{
                    co64= Co64.getBox(childHeader,reader,doLogging)
                }
                "stss"->{
                    stss= Stss.getBox(childHeader,reader,doLogging)
                }

            }

            // Ensure next child starts correctly
            reader.seek(
                childHeader.offset + childHeader.size
            )
        }


    }

    fun writeFutureStbl(output: RandomAccessFile,reader: RandomAccessFile,handlerType: String){
        val start = output.filePointer
        output.writeInt(0)
        output.write("stbl".toByteArray(Charsets.US_ASCII))

        val stsd=stsd?.getBoxAsBytes(reader)
        if (stsd!=null){
            output.write(stsd)
        }
        stts?.writeSttsBox(output)
        if (handlerType=="vide"){
            ctts?.writeCttsBox(output)
            stss?.writeStssBox(output)
        }
        val chunkPlan=computeChunkPlan(stsz?.sampleCount?.toInt()?:0)
        stsc?.writeStscBox(output,chunkPlan)
        stsz?.writeStszBox(output)
        stco?.writeStcoBox(output,chunkPlan)

        val end = output.filePointer
        val size = (end -start).toInt()

        output.seek(start)
        output.writeInt(size)
        output.seek(end)
    }

    private fun computeChunkPlan(totalSamples: Int): ChunkPlan {
        val first = 2
        val mid = 6

        val remaining = totalSamples - first
        val middleCount = if (remaining > 0) remaining / mid else 0
        val last = if (remaining > 0) remaining % mid else 0

        val chunkCount =
            1 + middleCount + if (last > 0) 1 else 0

        return ChunkPlan(first, mid, middleCount, last, chunkCount)
    }


    companion object {

        fun getBox(
            header: BoxHeader,
            reader: RandomAccessFile,
            doLogging: Boolean
        ): Stbl? {

            return try {
                Stbl(header,doLogging).apply {
                    parse(reader)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}
