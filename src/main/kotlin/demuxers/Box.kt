package demuxers

import java.io.RandomAccessFile

abstract class Box(
    val header: BoxHeader
) {
    protected abstract fun parse(reader: RandomAccessFile)
}