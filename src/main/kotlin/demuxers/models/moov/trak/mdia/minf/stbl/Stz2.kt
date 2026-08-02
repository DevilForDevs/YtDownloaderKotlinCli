package demuxers.models.moov.trak.mdia.minf.stbl

import demuxers.BoxHeader
import demuxers.models.moov.FullBox
import java.io.RandomAccessFile

class Stz2 private constructor(
    header: BoxHeader
) : FullBox(header) {

    var reserved: Int = 0
        private set

    var fieldSize: Int = 0
        private set

    var sampleCount: Long = 0
        private set

    val entries = mutableListOf<Int>()

    override fun parse(reader: RandomAccessFile) {

        parseFullBoxHeader(reader)

        reserved = reader.readUnsignedByte() shl 16 or
                (reader.readUnsignedByte() shl 8) or
                reader.readUnsignedByte()

        fieldSize = reader.readUnsignedByte()

        sampleCount = reader.readUnsignedInt()

        repeat(sampleCount.toInt()) {

            val sampleSize = when (fieldSize) {

                4 -> {
                    // Two entries packed into one byte.
                    // Handle with a nibble reader if needed.
                    0
                }

                8 -> reader.readUnsignedByte()

                16 -> reader.readUnsignedShort()

                else -> throw IllegalArgumentException("Unsupported fieldSize: $fieldSize")
            }

            /* entries.add(sampleSize) */
        }
    }

    companion object {

        fun getBox(
            header: BoxHeader,
            reader: RandomAccessFile,
            doLogging: Boolean
        ): Stz2? {

            return try {
                Stz2(header).apply {
                    parse(reader)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}