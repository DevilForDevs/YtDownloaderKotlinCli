package demuxers.models.moov.trak

import demuxers.Box
import demuxers.BoxHeader
import demuxers.models.moov.trak.edts.Elst
import demuxers.readBoxHeader
import java.io.RandomAccessFile

class Edts private constructor(
    header: BoxHeader,
    val doLogging: Boolean
) : Box(header) {
    var elst: Elst?=null

    override fun parse(reader: RandomAccessFile) {
        reader.seek(header.payloadOffset)
        while (reader.filePointer < header.offset + header.size) {

            val childHeader = readBoxHeader(reader) ?: break

            when(childHeader.type) {
                "elst" -> {
                    elst = Elst.getBox(childHeader, reader,doLogging)
                }
            }
            reader.seek(childHeader.offset + childHeader.size)
        }

    }

    companion object {

        fun getBox(
            header: BoxHeader,
            reader: RandomAccessFile,
            doLogging: Boolean
        ): Edts? {

            return try {
                Edts(header,doLogging).apply {
                    parse(reader)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}