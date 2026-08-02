package demuxers.models.moov.trak.mdia.minf

import demuxers.Box
import demuxers.BoxHeader
import demuxers.models.moov.trak.mdia.minf.dinf.Dref
import demuxers.readBoxHeader
import java.io.RandomAccessFile

class Dinf private constructor(
    header: BoxHeader
) : Box(header) {

    var dref: Dref? = null
        private set

    override fun parse(reader: RandomAccessFile) {

        reader.seek(header.payloadOffset)

        while (reader.filePointer < header.offset + header.size) {

            val childHeader = readBoxHeader(reader) ?: break

            when(childHeader.type) {
                "dref" -> {
                    dref = Dref.getBox(
                        childHeader,
                        reader
                    )
                }
            }

            reader.seek(childHeader.offset + childHeader.size)
        }

    }

    companion object {

        fun getBox(
            header: BoxHeader,
            reader: RandomAccessFile
        ): Dinf? {

            return try {
                Dinf(header).apply {
                    parse(reader)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}