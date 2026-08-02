package demuxers.models.moov.trak.mdia.minf.stbl.stsd

import demuxers.Box
import demuxers.BoxHeader
import demuxers.models.moov.trak.mdia.minf.stbl.stsd.avcc.AvcC
import demuxers.readBoxHeader
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile

class Avc1 private constructor(
    header: BoxHeader
) : Box(header) {

    var dataReferenceIndex: Int = 0
        private set

    var width: Int = 0
        private set

    var height: Int = 0
        private set

    var horizResolution: Long = 0
        private set

    var vertResolution: Long = 0
        private set

    var frameCount: Int = 0
        private set

    var compressorName: String = ""
        private set

    var depth: Int = 0
        private set

    var avcC: AvcC? = null
        private set

    val spsList = mutableListOf<ByteArray>()
    val ppsList = mutableListOf<ByteArray>()


    override fun parse(reader: RandomAccessFile) {

        reader.skipBytes(6)

        dataReferenceIndex = reader.readUnsignedShort()

        reader.skipBytes(16)

        width = reader.readUnsignedShort()
        height = reader.readUnsignedShort()

        horizResolution = reader.readUnsignedInt()
        vertResolution = reader.readUnsignedInt()

        reader.skipBytes(4)

        frameCount = reader.readUnsignedShort()

        val compressor = ByteArray(32)
        reader.readFully(compressor)

        val nameLength = compressor[0].toInt()

        compressorName =
            if (nameLength > 0) {
                String(compressor, 1, nameLength, Charsets.UTF_8)
            } else {
                ""
            }

        depth = reader.readUnsignedShort()

        reader.skipBytes(2)


        while (reader.filePointer < header.offset + header.size) {

            val childHeader = readBoxHeader(reader)
                ?: break

            when (childHeader.type) {

                "avcC" -> {
                    avcC = AvcC.getBox(
                        childHeader,
                        reader
                    )

                    avcC?.let {
                        spsList.addAll(it.sps)
                        ppsList.addAll(it.pps)
                    }
                }

                else -> {
                    reader.seek(
                        childHeader.offset + childHeader.size
                    )
                }
            }
        }
    }

    fun RandomAccessFile.readUnsignedInt(): Long {
        return readInt().toLong() and 0xFFFFFFFFL
    }

    companion object {

        fun getBox(
            header: BoxHeader,
            reader: RandomAccessFile
        ): Avc1? {

            return try {
                Avc1(header).apply {
                    parse(reader)
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    fun exportKeyFrame(
        fileNameWithoutExt: String,
        sample: ByteArray,
        folder: String,
        failures: (String) -> Unit
    ) {
        try {
            val dir = File(folder)

            if (!dir.exists()) {
                dir.mkdirs()
            }

            val h264File = File(dir, "$fileNameWithoutExt.h264")
            val jpgFile = File(dir, "$fileNameWithoutExt.jpg")

            // Convert MP4 AVC sample -> Annex B H264
            FileOutputStream(h264File).use { out ->

                fun startCode() {
                    out.write(byteArrayOf(0, 0, 0, 1))
                }

                val config = avcC
                    ?: throw IllegalStateException("Missing avcC")


                for (sps in config.sps) {
                    startCode()
                    out.write(sps)
                }


                for (pps in config.pps) {
                    startCode()
                    out.write(pps)
                }


                var offset = 0

                while (offset + config.nalLengthSize <= sample.size) {

                    var size = 0

                    repeat(config.nalLengthSize) {
                        size =
                            (size shl 8) or
                                    (sample[offset++].toInt() and 0xFF)
                    }

                    startCode()

                    out.write(
                        sample,
                        offset,
                        size
                    )

                    offset += size
                }
            }


            val process = ProcessBuilder(
                "ffmpeg",
                "-i",
                h264File.absolutePath,
                "-frames:v",
                "1",
                jpgFile.absolutePath,
                "-y"
            )
                .redirectErrorStream(true)
                .start()


            val exitCode = process.waitFor()

            if (exitCode != 0 || !jpgFile.exists()) {
                failures("FFmpeg failed exporting $fileNameWithoutExt")
            }


            h264File.delete()


        } catch (e: Exception) {

            failures(
                "Failed exporting $fileNameWithoutExt: ${e.message}"
            )
        }
    }
}