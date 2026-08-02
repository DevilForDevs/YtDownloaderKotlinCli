package muxers.models

import java.io.RandomAccessFile

abstract class ByteArrayBox(
    type: String
) : Box(type) {


    abstract fun payload(): ByteArray


    override fun size(): Long {
        return 8L + payload().size
    }


    override fun write(
        output: RandomAccessFile
    ) {

        val data = payload()

        writeHeader(
            output,
            data.size + 8L
        )

        output.write(data)
    }
}