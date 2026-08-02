package muxers.models

import java.io.RandomAccessFile

abstract class FullBox(
    type: String,
    var version: Int = 0,
    var flags: Int = 0
) : Box(type) {


    protected fun writeFullBoxHeader(
        output: RandomAccessFile
    ) {

        output.writeByte(version)

        output.writeByte(
            (flags shr 16) and 0xFF
        )

        output.writeByte(
            (flags shr 8) and 0xFF
        )

        output.writeByte(
            flags and 0xFF
        )
    }

    fun RandomAccessFile.writeUInt32(value: Long) {
        writeInt(
            (value and 0xFFFFFFFFL).toInt()
        )
    }


    protected fun fullBoxHeaderSize(): Int {
        return 4
    }
}