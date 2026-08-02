package demuxers.models.moov.trak.mdia

import demuxers.BoxHeader
import demuxers.TrakFilter
import demuxers.models.moov.FullBox
import java.io.RandomAccessFile

class Hdlr private constructor(
    header: BoxHeader,
    val doLogging: Boolean,
) : FullBox(header) {

    var preDefined: Long = 0
        private set

    var handlerType: String = ""
        private set

    var name: String = ""
        private set

    override fun parse(reader: RandomAccessFile) {

        parseFullBoxHeader(reader)

        preDefined = reader.readUnsignedInt()

        handlerType = reader.readFourCC()

        // reserved (3 × uint32)
        reader.skipBytes(12)

        val remaining = (
                header.payloadOffset +
                        header.payloadSize -
                        reader.filePointer
                ).toInt()

        if (remaining > 0) {
            name = reader.readNullTerminatedString(remaining)
        }
        if (doLogging) {
            println("hdlr")
            println("  handlerType = $handlerType")
            println("  name = \"$name\"")
        }
    }

    fun isRequiredTrak(trakFilter: TrakFilter?): Boolean {
        return when {
            trakFilter == null -> false
            trakFilter.handler != null -> trakFilter.handler.code == handlerType
            else -> trakFilter.handlers?.any { it.code == handlerType } == true
        }
    }


    companion object {

        fun getBox(
            header: BoxHeader,
            reader: RandomAccessFile,
            doLogging: Boolean,
            trakFilter: TrakFilter?=null
        ): Hdlr? {

            return try {
                val hdlr=Hdlr(header,doLogging)
                hdlr.parse(reader)
                if (hdlr.isRequiredTrak(trakFilter)){
                    return hdlr
                }
                return null
            } catch (e: Exception) {
                null
            }
        }
    }

    fun RandomAccessFile.readFourCC(): String {
        val bytes = ByteArray(4)
        readFully(bytes)
        return String(bytes, Charsets.US_ASCII)
    }

    fun getBoxAsBytes(reader: RandomAccessFile): ByteArray {
        val hdlrNew= ByteArray(header.size.toInt())
        reader.seek(header.offset)
        reader.readFully(hdlrNew)
        return hdlrNew
    }

    fun RandomAccessFile.readNullTerminatedString(maxLength: Int): String {

        val bytes = ByteArray(maxLength)
        readFully(bytes)

        val end = bytes.indexOf(0).let {
            if (it == -1) bytes.size else it
        }

        return String(bytes, 0, end, Charsets.UTF_8)
    }

}