package demuxers.models

import demuxers.Box
import demuxers.BoxHeader
import demuxers.TrakFilter
import demuxers.models.moov.Mvhd
import demuxers.models.moov.Trak
import demuxers.readBoxHeader
import java.io.RandomAccessFile

class Moov private constructor(
    header: BoxHeader,
    val doLogging: Boolean,
    val trakFilter: TrakFilter?=null,
   val endOffset: Long,
    val startOffset: Long,
) : Box(header) {

    var mvhd: Mvhd?=null
    val traks= mutableListOf<Trak>()

    override fun parse(reader: RandomAccessFile) {

        reader.seek(header.payloadOffset)

        while (reader.filePointer < header.offset + header.size) {
            val childHeader = readBoxHeader(reader) ?: break
            when (childHeader.type) {
                "mvhd" -> {
                    mvhd = Mvhd.getBox(childHeader, reader,doLogging)
                }
                "trak" -> {
                    if (mvhd!=null){
                        val trak= Trak.getBox(childHeader, reader, mvhd?.timescale,doLogging,trakFilter,endOffset,startOffset)
                        if (trak!=null){
                            traks.add(trak)
                        }
                    }
                }
            }

            // Move to next child box
            reader.seek(childHeader.offset + childHeader.size)
        }
    }

    companion object {
        fun getBox(
            header: BoxHeader,
            reader: RandomAccessFile,
            doLogging: Boolean,
            trakFilter: TrakFilter?=null,
            endOffset: Long,
            startOffset: Long,
        ): Moov? {

            return try {
                Moov(header,doLogging,trakFilter,endOffset,startOffset).apply {
                    parse(reader)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}