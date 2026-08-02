package muxers.models

import java.io.RandomAccessFile

abstract class Box(
    val type: String
) {

    abstract fun size(): Long

    abstract fun write(output: RandomAccessFile)


    protected fun writeHeader(
        output: RandomAccessFile,
        size: Long
    ) {
        output.writeInt(size.toInt())
        output.writeBytes(type)
    }
}