package muxers

import demuxers.models.moov.Mvhd
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

private fun writeUInt32(
    out: ByteArrayOutputStream,
    value: Long
) {
    out.write(((value shr 24) and 0xFF).toInt())
    out.write(((value shr 16) and 0xFF).toInt())
    out.write(((value shr 8) and 0xFF).toInt())
    out.write((value and 0xFF).toInt())
}

fun createSttsBox(
    durations: List<Long>
): ByteArray {

    val entries = mutableListOf<Pair<Long,Long>>()


    var last = durations[0]
    var count = 1L

    for (i in 1 until durations.size) {
        if (durations[i] == last) {
            count++
        } else {
            entries.add(count to last)
            last = durations[i]
            count = 1
        }
    }

    entries.add(count to last)

    val out = ByteArrayOutputStream()

    val size = 16 + entries.size * 8

    writeUInt32(out, size.toLong())
    out.write("stts".toByteArray(Charsets.US_ASCII))

    writeUInt32(out, 0)

    writeUInt32(out, entries.size.toLong())

    for ((sampleCount, delta) in entries) {
        writeUInt32(out, sampleCount)
        writeUInt32(out, delta)
    }

    return out.toByteArray()
}

fun createAudioSttsBox(sampleCount: Long): ByteArray {
    // Only one entry: all samples have duration 1024
    val entryCount = 1L
    val duration = 1024L

    val out = ByteArrayOutputStream()

    // Box size: 16 bytes header + 8 bytes per entry
    val size = 16 + (entryCount * 8)

    writeUInt32(out, size.toLong())
    out.write("stts".toByteArray(Charsets.US_ASCII))

    // Version (0) + flags (0)
    writeUInt32(out, 0)

    // Entry count
    writeUInt32(out, entryCount)

    // The single entry: (sampleCount, duration)
    writeUInt32(out, sampleCount)
    writeUInt32(out, duration)

    return out.toByteArray()
}



fun createStszBox(sampleSizes: List<Long>): ByteArray {
    val out = ByteArrayOutputStream()

    val size = 20 + sampleSizes.size * 4

    writeUInt32(out, size.toLong())
    out.write("stsz".toByteArray(Charsets.US_ASCII))

    // version + flags
    writeUInt32(out, 0)

    // sample_size (0 = sizes follow)
    writeUInt32(out, 0)

    // sample_count
    writeUInt32(out, sampleSizes.size.toLong())

    // sample sizes
    for (sampleSize in sampleSizes) {
        writeUInt32(out, sampleSize)
    }

    return out.toByteArray()
}

fun createStcoBox(chunkOffsets: List<Long>): ByteArray {
    val out = ByteArrayOutputStream()

    val size = 16 + chunkOffsets.size * 4

    writeUInt32(out, size.toLong())
    out.write("stco".toByteArray(Charsets.US_ASCII))

    // version + flags
    writeUInt32(out, 0)

    // entry_count
    writeUInt32(out, chunkOffsets.size.toLong())

    // chunk offsets
    for (offset in chunkOffsets) {
        writeUInt32(out, offset)
    }

    return out.toByteArray()
}

fun createDinfBox(): ByteArray {
    val out = ByteArrayOutputStream()

    // dinf(36) = header(8) + dref(28)
    writeUInt32(out, 36)
    out.write("dinf".toByteArray(Charsets.US_ASCII))

    // dref box (28 bytes)
    writeUInt32(out, 28)
    out.write("dref".toByteArray(Charsets.US_ASCII))

    // version + flags
    writeUInt32(out, 0)

    // entry_count
    writeUInt32(out, 1)

    // url box (12 bytes)
    writeUInt32(out, 12)
    out.write("url ".toByteArray(Charsets.US_ASCII))

    // version = 0, flags = 1 (self-contained)
    writeUInt32(out, 1)

    return out.toByteArray()
}

fun createStssBox(
    syncSampleIndices: List<Int>
): ByteArray {

    val out = ByteArrayOutputStream()

    val size = 16 + syncSampleIndices.size * 4

    // size
    writeUInt32(out, size.toLong())

    // type
    out.write("stss".toByteArray(Charsets.US_ASCII))

    // version + flags
    writeUInt32(out, 0)

    // entry_count
    writeUInt32(out, syncSampleIndices.size.toLong())

    // sample numbers
    for (index in syncSampleIndices) {
        writeUInt32(out, index.toLong())
    }

    return out.toByteArray()
}

fun createStblBox(
    stsd: ByteArray,
    stts: ByteArray,
    stsc: ByteArray,
    stsz: ByteArray,
    stco: ByteArray,
    stss: ByteArray? = null   // ← default to null
): ByteArray {

    val out = ByteArrayOutputStream()

    val size = 8 +
            stsd.size +
            stts.size +
            (stss?.size ?: 0) +
            stsc.size +
            stsz.size +
            stco.size

    writeUInt32(out, size.toLong())
    out.write("stbl".toByteArray(Charsets.US_ASCII))
    out.write(stsd)
    out.write(stts)

    if (stss != null) {
        out.write(stss)
    }

    out.write(stsc)
    out.write(stsz)
    out.write(stco)

    return out.toByteArray()
}

fun createVmhdBox(): ByteArray {
    val out = ByteArrayOutputStream()

    // box size
    writeUInt32(out, 20)

    // type
    out.write("vmhd".toByteArray(Charsets.US_ASCII))

    // version (0) + flags (1)
    writeUInt32(out, 1)

    // graphicsmode = 0 (copy)
    writeUInt16(out, 0)

    // opcolor = {0, 0, 0}
    writeUInt16(out, 0)
    writeUInt16(out, 0)
    writeUInt16(out, 0)

    return out.toByteArray()
}

private fun writeUInt16(
    out: ByteArrayOutputStream,
    value: Int
) {
    out.write((value shr 8) and 0xFF)
    out.write(value and 0xFF)
}

fun createMinfBox(
    mediaHeader: ByteArray, // vmhd or smhd
    dinf: ByteArray,
    stbl: ByteArray
): ByteArray {

    val out = ByteArrayOutputStream()

    val size = 8 +
            mediaHeader.size +
            dinf.size +
            stbl.size

    writeUInt32(out, size.toLong())
    out.write("minf".toByteArray(Charsets.US_ASCII))

    out.write(mediaHeader)
    out.write(dinf)
    out.write(stbl)

    return out.toByteArray()
}

fun createMdiaBox(
    mdhd: ByteArray,
    hdlr: ByteArray,
    minf: ByteArray
): ByteArray {

    val out = ByteArrayOutputStream()

    val size = 8 +
            mdhd.size +
            hdlr.size +
            minf.size

    // size
    writeUInt32(out, size.toLong())

    // type
    out.write("mdia".toByteArray(Charsets.US_ASCII))

    // children
    out.write(mdhd)
    out.write(hdlr)
    out.write(minf)

    return out.toByteArray()
}

fun createTrakBox(
    tkhd: ByteArray,
    edts: ByteArray?,
    mdia: ByteArray
): ByteArray {

    val out = ByteArrayOutputStream()

    val size = 8 +
            tkhd.size +
            (edts?.size ?: 0) +
            mdia.size

    // box size
    writeUInt32(out, size.toLong())

    // box type
    out.write("trak".toByteArray(Charsets.US_ASCII))

    // tkhd
    out.write(tkhd)

    // edts (optional)
    if (edts != null) {
        out.write(edts)
    }

    // mdia
    out.write(mdia)

    return out.toByteArray()
}

fun createMoovBox(
    mvhd: ByteArray,
    traks: List<ByteArray>
): ByteArray {

    val out = ByteArrayOutputStream()

    val size = 8 +
            mvhd.size +
            traks.sumOf { it.size }

    // box size
    writeUInt32(out, size.toLong())

    // box type
    out.write("moov".toByteArray(Charsets.US_ASCII))

    // movie header
    out.write(mvhd)

    // tracks
    for (trak in traks) {
        out.write(trak)
    }

    return out.toByteArray()
}

fun mp4EpochNow(): Int {
    val qtEpoch = 2082844800L   // 1904-01-01 → Unix epoch offset
    val now = System.currentTimeMillis() / 1000
    return (now + qtEpoch).toInt()
}
fun writeTkhd(duration: Long, width: Int, height: Int,trakId:Int): ByteArray {
    val size = 104
    val buffer = ByteBuffer.allocate(size).order(ByteOrder.BIG_ENDIAN)

    buffer.putInt(size)                     // box size
    buffer.put("tkhd".toByteArray())        // type

    buffer.put(1)                            // version = 1
    buffer.put(byteArrayOf(0x00, 0x00, 0x07)) // flags = enabled | movie | preview

    val macTime = (System.currentTimeMillis() / 1000) + 2082844800L

    buffer.putLong(macTime)                 // creation_time
    buffer.putLong(macTime)                 // modification_time

    buffer.putInt(trakId)                        // track_ID
    buffer.putInt(0)                        // reserved

    buffer.putLong(duration)                // duration 64-bit

    buffer.putInt(0)                        // reserved1
    buffer.putInt(0)                        // reserved2

    buffer.putShort(0)                      // layer
    buffer.putShort(0)                      // alternate group
    buffer.putShort(0)                      // volume (0 for video)
    buffer.putShort(0)                      // reserved

    // Identity matrix
    val matrix = intArrayOf(
        0x00010000, 0, 0,
        0, 0x00010000, 0,
        0, 0, 0x40000000
    )
    matrix.forEach { buffer.putInt(it) }

    buffer.putInt(width shl 16)             // width 16.16
    buffer.putInt(height shl 16)            // height 16.16

    return buffer.array()
}


fun writeMvhd(mvhd: Mvhd, timeScale:Int?, duration: Int?): ByteArray {
    val size = 8 + 100
    val bb = ByteBuffer.allocate(size).order(ByteOrder.BIG_ENDIAN)

    val creationTime = mp4EpochNow()

    bb.putInt(size)
    bb.put("mvhd".toByteArray(Charsets.US_ASCII))
    bb.put(0)
    bb.put(ByteArray(3))

    bb.putInt(creationTime)
    bb.putInt(creationTime)

    bb.putInt(timeScale?:mvhd.timescale.toInt())
    bb.putInt(duration?:mvhd.duration.toInt())

    bb.putInt(0x00010000)
    bb.putShort(0x0100.toShort())
    bb.putShort(0)
    bb.putInt(0)
    bb.putInt(0)

    bb.putInt(0x00010000); bb.putInt(0); bb.putInt(0)
    bb.putInt(0); bb.putInt(0x00010000); bb.putInt(0)
    bb.putInt(0); bb.putInt(0); bb.putInt(0x40000000)

    bb.putInt(2)  // nextTrackID

    return bb.array()
}