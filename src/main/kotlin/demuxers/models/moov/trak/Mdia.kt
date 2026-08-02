package demuxers.models.moov.trak

import demuxers.Box
import demuxers.BoxHeader
import demuxers.TrakFilter
import demuxers.models.moov.trak.mdia.Hdlr
import demuxers.models.moov.trak.mdia.Mdhd
import demuxers.models.moov.trak.mdia.Minf
import demuxers.readBoxHeader
import java.io.RandomAccessFile

class Mdia private constructor(
    header: BoxHeader,
    val doLogging: Boolean,
    val trakFilter: TrakFilter?=null
) : Box(header) {
    var mdhd: Mdhd?=null
    var hdlr: Hdlr?=null
    var minf: Minf?=null

    override fun parse(reader: RandomAccessFile) {
        reader.seek(header.payloadOffset)

        while (reader.filePointer < header.offset + header.size) {

            val childHeader = readBoxHeader(reader) ?: break
            when(childHeader.type) {
                "mdhd"->{
                    mdhd= Mdhd.getBox(childHeader,reader,doLogging)
                }
                "hdlr"->{
                   hdlr= Hdlr.getBox(childHeader,reader,doLogging,trakFilter)
                    if (hdlr==null){
                        break
                    }
                }
                "minf"->{
                    minf= Minf.getBox(childHeader,reader,doLogging)
                }
            }

            reader.seek(childHeader.offset + childHeader.size)
        }


    }

    fun writeFutureMdia(output: RandomAccessFile,reader: RandomAccessFile){
        val mdiaStart = output.filePointer
        output.writeInt(0)
        output.write("mdia".toByteArray(Charsets.US_ASCII))

        val mdhd=mdhd?.getBoxAsBytes(reader)
        if (mdhd!=null){
            output.write(mdhd)
        }

        val hdlr=hdlr?.getBoxAsBytes(reader)
        if (hdlr!=null){
            output.write(hdlr)
        }

        minf?.writeFutureMinf(output,reader,this.hdlr?.handlerType?:"")

        val mdiaEnd = output.filePointer
        val mdiaSize = (mdiaEnd - mdiaStart).toInt()
        output.seek(mdiaStart)
        output.writeInt(mdiaSize)
        output.seek(mdiaEnd)

    }

    companion object {

        fun getBox(
            header: BoxHeader,
            reader: RandomAccessFile,
            doLogging: Boolean,
            trakFilter: TrakFilter?=null
        ): Mdia? {

            return try {
                val md=Mdia(header,doLogging,trakFilter)
                md.parse(reader)
                if (md.hdlr!=null){
                   return md
                }
                return null
            } catch (e: Exception) {
                null
            }
        }
    }
}