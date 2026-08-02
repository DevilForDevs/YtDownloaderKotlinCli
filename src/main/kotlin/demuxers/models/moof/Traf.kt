package demuxers.models.moof

import demuxers.Box
import demuxers.BoxHeader
import demuxers.models.moof.traf.Tfdt
import demuxers.models.moof.traf.Tfhd
import demuxers.models.moof.traf.Trun
import demuxers.models.moov.Trak
import demuxers.readBoxHeader
import java.io.RandomAccessFile

class Traf private constructor(
    header: BoxHeader,
    val moovTraks: MutableList<Trak>?
) : Box(header) {

    var tfhd: Tfhd? = null
        private set

    var tfdt: Tfdt? = null
        private set



    override fun parse(reader: RandomAccessFile) {

        reader.seek(header.payloadOffset)

        while (reader.filePointer < header.offset + header.size) {

            val childHeader = readBoxHeader(reader)
                ?: break

            when (childHeader.type) {

                "tfhd" -> {
                    tfhd = Tfhd.getBox(
                        childHeader,
                        reader
                    )
                }

                "tfdt" -> {
                    tfdt = Tfdt.getBox(
                        childHeader,
                        reader
                    )
                }

                "trun" -> {

                    val requiredTrak=moovTraks?.find { it.tkhd?.trackId==tfhd?.trackId }
                    val trakType=requiredTrak?.mdia?.hdlr?.handlerType
                    Trun.getBox(
                        childHeader,
                        reader,
                        tfdt?.baseMediaDecodeTime,
                        tfhd,
                        requiredTrak?.mdia?.minf?.stbl,
                        trakType?:""
                    )



                }
            }

            reader.seek(
                childHeader.offset + childHeader.size
            )
        }
    }

    fun getTruns(
        header: BoxHeader,
        reader: RandomAccessFile,
        trakId: Long?,
        moofOffset: Long
    ): MutableList<Trun> {
        val truns=mutableListOf<Trun>()
        reader.seek(header.payloadOffset)

        while (reader.filePointer < header.offset + header.size) {

            val childHeader = readBoxHeader(reader)
                ?: break

            when (childHeader.type) {
                "tfhd"->{
                    tfhd=Tfhd.getBox(childHeader,reader)
                    if (trakId!=tfhd?.trackId){
                        return truns
                    }
                }
                "trun"->{
                    val trun= Trun.getBox(childHeader,reader,moofOffset,tfhd)
                    if (trun!=null){
                        truns.add(trun)
                    }
                }
            }
            reader.seek(childHeader.offset + childHeader.size)
        }
        return truns
    }


    companion object {

        fun getBox(
            header: BoxHeader,
            reader: RandomAccessFile,
            moovTraks: MutableList<Trak>?
        ): Traf? {

            return try {
                Traf(header,moovTraks).apply {
                    parse(reader)
                }
            } catch (e: Exception) {
                null
            }
        }

        fun getTruns(
            header: BoxHeader,
            reader: RandomAccessFile,
            trakId: Long?,
            moofOffset: Long
        ): MutableList<Trun>{
            val traf= Traf(header,null)
            return traf.getTruns(header,reader,trakId,moofOffset)
        }

    }
}