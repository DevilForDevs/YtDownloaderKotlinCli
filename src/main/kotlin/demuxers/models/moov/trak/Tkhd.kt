package demuxers.models.moov.trak

import demuxers.BoxHeader
import demuxers.formatDuration
import demuxers.formatMp4Time
import demuxers.models.moov.FullBox
import java.io.RandomAccessFile

class Tkhd(header: BoxHeader,private val mvhdTimeScale: Long?,val doLogging: Boolean,) : FullBox(header) {

    var creationTime: Long = 0
        private set

    var modificationTime: Long = 0
        private set

    var trackId: Long = 0
        private set

    var duration: Long = 0
        private set

    var layer: Int = 0
        private set

    var alternateGroup: Int = 0
        private set

    var volume: Int = 0
        private set

    var width: Double = 0.0
        private set

    var height: Double = 0.0
        private set
    val creationDate: String
        get() = formatMp4Time(creationTime)

    val modificationDate: String
        get() = formatMp4Time(modificationTime)
    val durationFormatted: String
        get() = mvhdTimeScale?.let { scale ->
            formatDuration(duration, scale)
        } ?: "Unknown"


    override fun parse(reader: RandomAccessFile) {

        reader.seek(header.payloadOffset)

        parseFullBoxHeader(reader)

        if (version == 1) {

            creationTime = reader.readLong()
            modificationTime = reader.readLong()
            trackId = reader.readInt().toLong() and 0xFFFFFFFFL

            // reserved
            reader.skipBytes(4)

            duration = reader.readLong()

        } else {

            creationTime = reader.readInt()
                .toLong() and 0xFFFFFFFFL

            modificationTime = reader.readInt()
                .toLong() and 0xFFFFFFFFL

            trackId = reader.readInt()
                .toLong() and 0xFFFFFFFFL

            // reserved
            reader.skipBytes(4)

            duration = reader.readInt()
                .toLong() and 0xFFFFFFFFL
        }


        // reserved
        reader.skipBytes(8)

        layer = reader.readShort().toInt()

        alternateGroup = reader.readShort().toInt()

        // 8.8 fixed point
        volume = reader.readUnsignedShort()

        // reserved
        reader.skipBytes(2)

        // matrix (36 bytes)
        reader.skipBytes(36)

        // 16.16 fixed point
        width = reader.readInt() / 65536.0
        height = reader.readInt() / 65536.0
    }


    companion object {

        fun getBox(
            header: BoxHeader,
            reader: RandomAccessFile,
            mvhdTimeScalep: Long?,
            doLogging: Boolean,
        ): Tkhd? {
            return try {
                Tkhd(header,mvhdTimeScalep,doLogging).apply {
                    parse(reader)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}