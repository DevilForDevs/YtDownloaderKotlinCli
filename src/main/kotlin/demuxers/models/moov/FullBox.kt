package demuxers.models.moov

import demuxers.Box
import demuxers.BoxHeader
import java.io.RandomAccessFile

abstract class FullBox(header: BoxHeader) : Box(header) {
    var version: Int = 0
    var flags: Int = 0

    fun parseFullBoxHeader(reader: RandomAccessFile) {
        version = reader.readUnsignedByte()
        flags =
            (reader.readUnsignedByte() shl 16) or
                    (reader.readUnsignedByte() shl 8) or
                    reader.readUnsignedByte()
    }
    fun RandomAccessFile.readUnsignedInt(): Long {
        return readInt().toLong() and 0xFFFFFFFFL
    }
}