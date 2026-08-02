package demuxers.models.moov.trak.mdia.minf.stbl

import demuxers.BoxHeader
import demuxers.models.moov.FullBox
import demuxers.models.moov.trak.mdia.minf.stbl.stsd.Av01
import demuxers.models.moov.trak.mdia.minf.stbl.stsd.Avc1
import demuxers.readBoxHeader
import java.io.RandomAccessFile

class Stsd private constructor(
    header: BoxHeader
) : FullBox(header) {

    var entryCount: Long = 0
        private set
    var avc1: Avc1?=null
    var av01: Av01?=null



    override fun parse(reader: RandomAccessFile) {

        parseFullBoxHeader(reader)

        entryCount = reader.readUnsignedInt()

        repeat(entryCount.toInt()) {

            val childHeader = readBoxHeader(reader)
                ?: return@repeat

            val entry = when (childHeader.type) {

                "avc1" -> {
                    avc1= Avc1.getBox(
                        childHeader,
                        reader
                    )
                }
                "av01"->{
                    av01= Av01.getBox(childHeader,reader)
                }

                else -> {
                    reader.seek(
                        childHeader.offset + childHeader.size
                    )
                    null
                }
            }
        }
    }

    fun getBoxAsBytes(reader: RandomAccessFile): ByteArray {
        val stsdBytes= ByteArray(header.size.toInt())
        reader.seek(header.offset)
        reader.readFully(stsdBytes)
        return stsdBytes
    }

    fun exportKeyFrame(
        fileNameWithoutExt: String,
        sample: ByteArray,
        folder: String,
        failures: (String) -> Unit
    ) {
        if (avc1!=null){
            avc1!!.exportKeyFrame(
                fileNameWithoutExt,
                sample,
                folder,
                failures
            )
        }
        if (av01!=null){
            av01!!.exportKeyFrame(
                fileNameWithoutExt,
                sample,
                folder,
                failures
            )
        }
    }

    companion object {

        fun getBox(
            header: BoxHeader,
            reader: RandomAccessFile,
            doLogging: Boolean
        ): Stsd? {

            return try {
                Stsd(header).apply {
                    parse(reader)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}