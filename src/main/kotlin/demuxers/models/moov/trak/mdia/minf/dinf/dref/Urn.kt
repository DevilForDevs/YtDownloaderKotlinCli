package demuxers.models.moov.trak.mdia.minf.dinf.dref

import demuxers.BoxHeader
import demuxers.models.moov.FullBox
import java.io.RandomAccessFile

class Urn private constructor(
    header: BoxHeader
) : FullBox(header) {

    var name: String = ""
        private set

    var location: String = ""
        private set


    override fun parse(reader: RandomAccessFile) {

        parseFullBoxHeader(reader)

        val remaining = (
                header.payloadOffset +
                        header.payloadSize -
                        reader.filePointer
                ).toInt()

        if (remaining <= 0) return

        val bytes = ByteArray(remaining)
        reader.readFully(bytes)

        val parts = bytes
            .toString(Charsets.UTF_8)
            .split("\u0000")

        name = parts.getOrNull(0) ?: ""
        location = parts.getOrNull(1) ?: ""
    }


    companion object {

        fun getBox(
            header: BoxHeader,
            reader: RandomAccessFile
        ): Urn? {

            return try {
                Urn(header).apply {
                    parse(reader)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}