package muxers.models

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.RandomAccessFile

class Ftyp(
    private val brands: List<String>
) : ByteArrayBox("ftyp") {


    override fun payload(): ByteArray {

        val buffer = ByteArrayOutputStream()
        val output = DataOutputStream(buffer)


        output.writeBytes("isom")

        output.writeInt(512)


        brands.forEach {
            require(it.length == 4)
            output.writeBytes(it)
        }


        output.flush()

        return buffer.toByteArray()
    }
}