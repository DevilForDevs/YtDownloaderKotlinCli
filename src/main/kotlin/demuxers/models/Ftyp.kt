package demuxers.models

import demuxers.Box
import demuxers.BoxHeader
import java.io.RandomAccessFile

class Ftyp private constructor(
    header: BoxHeader,
    doLogging: Boolean,
) : Box(header) {

    lateinit var majorBrand: String
        private set

    var minorVersion: Long = 0
        private set

    val compatibleBrands = mutableListOf<String>()

    override fun parse(reader: RandomAccessFile) {

        reader.seek(header.payloadOffset)

        majorBrand = ByteArray(4)
            .also(reader::readFully)
            .toString(Charsets.US_ASCII)

        minorVersion = reader.readInt().toLong() and 0xFFFFFFFFL

        while (reader.filePointer + 4 <= header.offset + header.size) {
            compatibleBrands += ByteArray(4)
                .also(reader::readFully)
                .toString(Charsets.US_ASCII)
        }
    }

    companion object {

        fun getBox(
            header: BoxHeader,
            reader: RandomAccessFile,
             doLogging: Boolean,
        ): Ftyp? {

            return try {
                Ftyp(header, doLogging).apply {
                    parse(reader)
                }
            } catch (e: Exception) {
                null
            }
        }
    }

}