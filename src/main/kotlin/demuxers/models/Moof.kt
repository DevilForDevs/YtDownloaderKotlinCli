package demuxers.models

import demuxers.Box
import demuxers.BoxHeader
import demuxers.SampleInfo
import demuxers.models.moof.Mfhd
import demuxers.models.moof.Traf
import demuxers.models.moof.traf.Trun
import demuxers.models.moov.Trak
import demuxers.readBoxHeader
import java.io.RandomAccessFile

class Moof private constructor(
    header: BoxHeader,
    val moovTraks: MutableList<Trak>?,
) : Box(header) {
    var mfhd: Mfhd?=null

    override fun parse(reader: RandomAccessFile) {

        reader.seek(header.payloadOffset)

        while (reader.filePointer < header.offset + header.size) {
            val childHeader = readBoxHeader(reader) ?: break
            when(childHeader.type){
                "mfhd"->{
                    mfhd= Mfhd.getBox(childHeader,reader)
                }
                "traf"->{
                   Traf.getBox(childHeader,reader,moovTraks)
                }
            }

            // Move to next child box
            reader.seek(childHeader.offset + childHeader.size)
        }
    }

    fun getTruns(reader: RandomAccessFile,trakId: Long?):MutableList<Trun>{
        reader.seek(header.payloadOffset)
        val truns = mutableListOf<Trun>()
        while (reader.filePointer < header.offset + header.size) {
            val childHeader = readBoxHeader(reader) ?: break
            when(childHeader.type){
                "traf"->{
                    truns += Traf.getTruns(childHeader, reader, trakId,header.offset)
                }
            }
            // Move to next child box
            reader.seek(childHeader.offset + childHeader.size)
        }
        return truns
    }

    companion object {
        fun getBox(
            header: BoxHeader,
            reader: RandomAccessFile,
            moovTraks: MutableList<Trak>?
        ): Moof? {

            return try {
                Moof(header,moovTraks).apply {
                    parse(reader)
                }
            } catch (e: Exception) {
                null
            }
        }

        fun getTruns(
            header: BoxHeader,
            reader: RandomAccessFile,
            trakId: Long?

        ):MutableList<Trun>{
            val moof= Moof(header,null)
            return moof.getTruns(reader,trakId)
        }
    }

}