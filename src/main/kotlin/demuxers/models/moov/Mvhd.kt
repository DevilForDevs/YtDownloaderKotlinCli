package demuxers.models.moov

import demuxers.BoxHeader
import demuxers.formatDuration
import demuxers.formatMp4Time
import java.io.RandomAccessFile

class Mvhd(header: BoxHeader,doLogging: Boolean) : FullBox(header) {

    var creationTime: Long = 0
        private set

    var modificationTime: Long = 0
        private set

    val creationDate: String
        get() = formatMp4Time(creationTime)

    val modificationDate: String
        get() = formatMp4Time(modificationTime)

    var timescale: Long = 0
        private set

    var duration: Long = 0
        private set

    val durationFormatted: String
        get() = formatDuration(duration, timescale)

    var rate: Int = 0
        private set

    var volume: Int = 0
        private set

    var nextTrackId: Long = 0
        private set


    override fun parse(reader: RandomAccessFile) {

        reader.seek(header.payloadOffset)

        parseFullBoxHeader(reader)

        if (version == 1) {

            creationTime = reader.readLong()
            modificationTime = reader.readLong()

            timescale = reader.readInt()
                .toLong() and 0xFFFFFFFFL

            duration = reader.readLong()

        } else {

            creationTime = reader.readInt()
                .toLong() and 0xFFFFFFFFL

            modificationTime = reader.readInt()
                .toLong() and 0xFFFFFFFFL

            timescale = reader.readInt()
                .toLong() and 0xFFFFFFFFL

            duration = reader.readInt()
                .toLong() and 0xFFFFFFFFL
        }

        rate = reader.readInt()

        volume = reader.readUnsignedShort()

        reader.skipBytes(10)
        reader.skipBytes(36)
        reader.skipBytes(24)

        nextTrackId = reader.readInt()
            .toLong() and 0xFFFFFFFFL
    }





    companion object {

        fun getBox(
            header: BoxHeader,
            reader: RandomAccessFile,
            doLogging: Boolean,
        ): Mvhd? {

            return try {
                Mvhd(header,doLogging).apply {
                    parse(reader)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}