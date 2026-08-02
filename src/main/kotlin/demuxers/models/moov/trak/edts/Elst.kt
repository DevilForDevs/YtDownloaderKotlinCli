package demuxers.models.moov.trak.edts

import demuxers.BoxHeader
import demuxers.models.moov.FullBox
import java.io.RandomAccessFile

class Elst private constructor(
    header: BoxHeader,
    val doLogging: Boolean
) : FullBox(header) {

    data class Entry(
        val segmentDuration: Long,
        val mediaTime: Long,
        val mediaRateInteger: Short,
        val mediaRateFraction: Short
    )

    val entries = mutableListOf<Entry>()



    override fun parse(reader: RandomAccessFile) {

        parseFullBoxHeader(reader)

        val entryCount = reader.readInt()

        repeat(entryCount) {

            val segmentDuration: Long
            val mediaTime: Long

            if (version == 1) {
                segmentDuration = reader.readLong()
                mediaTime = reader.readLong()
            } else {
                segmentDuration = reader.readUnsignedInt()
                mediaTime = reader.readInt().toLong()
            }

            val mediaRateInteger = reader.readShort()
            val mediaRateFraction = reader.readShort()

            entries.add(
                Entry(
                    segmentDuration,
                    mediaTime,
                    mediaRateInteger,
                    mediaRateFraction
                )
            )
        }
    }


    companion object {

        fun getBox(
            header: BoxHeader,
            reader: RandomAccessFile,
            doLogging: Boolean
        ): Elst? {

            return try {
                Elst(header,doLogging).apply {
                    parse(reader)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}