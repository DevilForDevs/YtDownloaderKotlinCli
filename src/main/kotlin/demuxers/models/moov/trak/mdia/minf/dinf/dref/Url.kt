package demuxers.models.moov.trak.mdia.minf.dinf.dref

import demuxers.BoxHeader
import demuxers.models.moov.FullBox
import java.io.RandomAccessFile

class Url private constructor(
    header: BoxHeader
) : FullBox(header) {

    var location: String = ""
        private set

    override fun parse(reader: RandomAccessFile) {

        parseFullBoxHeader(reader)

        // self-contained data reference
        if (flags == 1) {
            location = ""
            return
        }

        val remaining = (
                header.payloadOffset +
                        header.payloadSize -
                        reader.filePointer
                ).toInt()

        if (remaining > 0) {

            val bytes = ByteArray(remaining)
            reader.readFully(bytes)

            location = String(
                bytes,
                Charsets.UTF_8
            ).trimEnd('\u0000')
        }
    }


    companion object {

        fun getBox(
            header: BoxHeader,
            reader: RandomAccessFile
        ): Url? {

            return try {
                Url(header).apply {
                    parse(reader)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}