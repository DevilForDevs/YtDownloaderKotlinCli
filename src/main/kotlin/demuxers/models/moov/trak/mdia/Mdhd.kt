package demuxers.models.moov.trak.mdia

import demuxers.BoxHeader
import demuxers.models.moov.FullBox
import java.io.RandomAccessFile

class Mdhd private constructor(
    header: BoxHeader,
   val doLogging: Boolean
) : FullBox(header) {

    var creationTime: Long = 0
        private set

    var modificationTime: Long = 0
        private set

    var timescale: Long = 0
        private set

    var duration: Long = 0
        private set

    var language: String = ""
        private set



    override fun parse(reader: RandomAccessFile) {

       parseFullBoxHeader(reader)

        if (version == 1) {

            creationTime = reader.readLong()
            modificationTime = reader.readLong()
            timescale = reader.readUnsignedInt()
            duration = reader.readLong()

        } else {

            creationTime = reader.readUnsignedInt()
            modificationTime = reader.readUnsignedInt()
            timescale = reader.readUnsignedInt()
            duration = reader.readUnsignedInt()
        }

        val languageBits = reader.readUnsignedShort()

        language = decodeLanguage(languageBits)

        reader.readShort()
    }

    private fun decodeLanguage(value: Int): String {

        val c1 = ((value shr 10) and 0x1F) + 0x60
        val c2 = ((value shr 5) and 0x1F) + 0x60
        val c3 = (value and 0x1F) + 0x60

        return "${c1.toChar()}${c2.toChar()}${c3.toChar()}"
    }

    fun getBoxAsBytes(reader: RandomAccessFile): ByteArray {
        val mdhdNew= ByteArray(header.size.toInt())
        reader.seek(header.offset)
        reader.readFully(mdhdNew)
        return mdhdNew
    }


    companion object {

        fun getBox(
            header: BoxHeader,
            reader: RandomAccessFile,
            doLogging: Boolean
        ): Mdhd? {

            return try {
                Mdhd(header,doLogging).apply {
                    parse(reader)
                }
            } catch (e: Exception) {
                null
            }
        }
    }

}