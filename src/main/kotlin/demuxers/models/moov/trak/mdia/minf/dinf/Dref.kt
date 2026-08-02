package demuxers.models.moov.trak.mdia.minf.dinf

import demuxers.Box
import demuxers.BoxHeader
import demuxers.models.moov.FullBox
import demuxers.models.moov.trak.mdia.minf.dinf.dref.Url
import demuxers.models.moov.trak.mdia.minf.dinf.dref.Urn
import demuxers.readBoxHeader
import java.io.RandomAccessFile

class Dref private constructor(
    header: BoxHeader
) : FullBox(header) {

    var entryCount: Long = 0
        private set

    var url: Url? = null
        private set

    var urn: Urn? = null
        private set



    override fun parse(reader: RandomAccessFile) {

        parseFullBoxHeader(reader)

        entryCount = reader.readUnsignedInt()

        repeat(entryCount.toInt()) {

            val childHeader = readBoxHeader(reader)
                ?: return@repeat

            when (childHeader.type) {

                "url " -> {
                    url = Url.getBox(
                        childHeader,
                        reader
                    )
                }

                "urn " -> {
                    urn = Urn.getBox(
                        childHeader,
                        reader
                    )
                }

                else -> {
                    reader.seek(
                        childHeader.offset + childHeader.size
                    )
                }
            }
        }
    }


    companion object {

        fun getBox(
            header: BoxHeader,
            reader: RandomAccessFile
        ): Dref? {

            return try {
                Dref(header).apply {
                    parse(reader)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}