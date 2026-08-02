package demuxers.models.moov.trak.mdia

import demuxers.Box
import demuxers.BoxHeader
import demuxers.models.moov.trak.mdia.minf.*
import demuxers.readBoxHeader
import java.io.RandomAccessFile

class Minf private constructor(
    header: BoxHeader,
    val doLogging: Boolean
) : Box(header) {

    var vmhd: Vmhd? = null
        private set

    var smhd: Smhd? = null
        private set

    var hmhd: Hmhd? = null
        private set

    var nmhd: Nmhd? = null
        private set

    var dinf: Dinf? = null
        private set
    var stbl: Stbl? = null
        private set


    override fun parse(reader: RandomAccessFile) {

        reader.seek(header.payloadOffset)

        while (reader.filePointer < header.offset + header.size) {

            val childHeader = readBoxHeader(reader) ?: break

            when (childHeader.type) {

                "vmhd" -> {
                    vmhd = Vmhd.getBox(
                        childHeader,
                        reader,
                        doLogging
                    )
                }

                "smhd" -> {
                    smhd = Smhd.getBox(
                        childHeader,
                        reader,
                        doLogging
                    )
                }

                "hmhd" -> {
                    hmhd = Hmhd.getBox(
                        childHeader,
                        reader,
                        doLogging
                    )
                }

                "nmhd" -> {
                    nmhd = Nmhd.getBox(
                        childHeader,
                        reader,
                        doLogging
                    )
                }

                "dinf" -> {
                    dinf = Dinf.getBox(
                        childHeader,
                        reader
                    )
                }
                "stbl"->{
                    stbl= Stbl.getBox(childHeader,reader,doLogging)
                }
            }

            // Ensure next child starts correctly
            reader.seek(
                childHeader.offset + childHeader.size
            )
        }
    }

    fun getMediaHeaderAsBytes(reader: RandomAccessFile): ByteArray? {
        if (vmhd!=null){
            val newMediaHeader= ByteArray(vmhd!!.header.size.toInt())
            reader.seek(vmhd!!.header.offset)
            reader.readFully(newMediaHeader)
            return newMediaHeader
        }
        if (smhd!=null){
            val newMediaHeader= ByteArray(smhd!!.header.size.toInt())
            reader.seek(smhd!!.header.offset)
            reader.readFully(newMediaHeader)
            return newMediaHeader
        }

        return null


    }

    fun getDinfAsBytes(reader: RandomAccessFile): ByteArray? {
        val dinfBox = dinf ?: return null

        val size = dinfBox.header.size.toInt()
        if (size <= 0) return null

        val data = ByteArray(size)
        reader.seek(dinfBox.header.offset)
        reader.readFully(data)

        return data
    }

    fun writeFutureMinf(output: RandomAccessFile,reader: RandomAccessFile,handlerType: String){
        val minfStart = output.filePointer
        output.writeInt(0)
        output.write("minf".toByteArray(Charsets.US_ASCII))

        val mediaHeader=getMediaHeaderAsBytes(reader)
        if (mediaHeader!=null){
            output.write(mediaHeader)
        }

        val dinfBox=getDinfAsBytes(reader)
        if (dinfBox!=null){
            output.write(dinfBox)
        }

        stbl?.writeFutureStbl(output,reader,handlerType)

        val minfEnd = output.filePointer
        val minfSize = (minfEnd - minfStart).toInt()

        output.seek(minfStart)
        output.writeInt(minfSize)
        output.seek(minfEnd)
    }


    companion object {

        fun getBox(
            header: BoxHeader,
            reader: RandomAccessFile,
            doLogging: Boolean
        ): Minf? {

            return try {
                Minf(header,doLogging).apply {
                    parse(reader)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}
