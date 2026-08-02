package muxers.models

import java.io.RandomAccessFile

abstract class ContainerBox(
    type: String
) : Box(type) {


    protected val children =
        mutableListOf<Box>()


    fun addBox(box: Box) {
        children.add(box)
    }


    override fun size(): Long {

        var total = 8L

        children.forEach {
            total += it.size()
        }

        return total
    }


    override fun write(
        output: RandomAccessFile
    ) {

        writeHeader(
            output,
            size()
        )

        children.forEach {
            it.write(output)
        }
    }
}