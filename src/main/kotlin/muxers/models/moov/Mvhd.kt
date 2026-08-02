package muxers.models.moov

import muxers.models.FullBox
import java.io.RandomAccessFile

class Mvhd(
    private val timescale: Long,
    private val duration: Long,
    private val creationTime: Long = 0,
    private val modificationTime: Long = 0
) : FullBox(
    type = "mvhd",
    version = 0,
    flags = 0
) {


    override fun size(): Long {
        // header 8
        // fullbox header 4
        // version 0 fields
        return 108
    }


    override fun write(
        output: RandomAccessFile
    ) {

        writeHeader(
            output,
            size()
        )

        writeFullBoxHeader(output)


        // creation_time
        output.writeUInt32(
            creationTime
        )


        // modification_time
        output.writeUInt32(
            modificationTime
        )


        // movie timescale
        output.writeUInt32(
            timescale
        )


        // duration
        output.writeUInt32(
            duration
        )


        // rate 16.16 fixed point = 1.0
        output.writeInt(
            0x00010000
        )


        // volume 8.8 fixed point = 1.0
        output.writeShort(
            0x0100
        )


        // reserved 10 bytes
        repeat(10) {
            output.writeByte(0)
        }


        // unity matrix
        val matrix = intArrayOf(
            0x00010000, 0, 0,
            0, 0x00010000, 0,
            0, 0, 0x40000000
        )

        matrix.forEach {
            output.writeInt(it)
        }


        // pre_defined 24 bytes
        repeat(6) {
            output.writeInt(0)
        }


        // next_track_ID
        output.writeUInt32(1)
    }
}