package demuxers.models.moov.trak.mdia.minf.stbl.stsd

import demuxers.Box
import demuxers.BoxHeader
import demuxers.models.moov.trak.mdia.minf.stbl.stsd.av01.Av1C
import demuxers.readBoxHeader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile

class Av01 private constructor(
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

    var av1C: Av1C? = null
        private set


    override fun parse(reader: RandomAccessFile) {

        // reserved 6 bytes
        reader.skipBytes(6)

        // data_reference_index
        dataReferenceIndex = reader.readUnsignedShort()

        // pre_defined + reserved
        reader.skipBytes(16)

        width = reader.readUnsignedShort()

        height = reader.readUnsignedShort()

        // 16.16 fixed point
        horizResolution = reader.readUnsignedInt()

        vertResolution = reader.readUnsignedInt()

        // reserved
        reader.skipBytes(4)

        frameCount = reader.readUnsignedShort()


        val compressor = ByteArray(32)
        reader.readFully(compressor)

        val nameLength = compressor[0].toInt()

        compressorName =
            if (nameLength > 0) {
                String(
                    compressor,
                    1,
                    nameLength,
                    Charsets.UTF_8
                )
            } else {
                ""
            }


        depth = reader.readUnsignedShort()

        // pre_defined = -1
        reader.skipBytes(2)


        // child boxes (av1C, pasp, colr...)
        while (reader.filePointer < header.offset + header.size) {

            val childHeader = readBoxHeader(reader)
                ?: break


            when (childHeader.type) {

                "av1C" -> {
                    av1C = Av1C.getBox(
                        childHeader,
                        reader
                    )
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
        ): Av01? {

            return try {
                Av01(header).apply {
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

            val av1File = File(dir, "$fileNameWithoutExt.av1")
            val jpgFile = File(dir, "$fileNameWithoutExt.jpg")

            // write temporary AV1 sample
            FileOutputStream(av1File).use { out ->
                out.write(sample)
            }


            val process = ProcessBuilder(
                "ffmpeg",
                "-c:v",
                "libdav1d",
                "-i",
                av1File.absolutePath,
                "-frames:v",
                "1",
                jpgFile.absolutePath,
                "-y"
            )
                .redirectErrorStream(true)
                .start()


            val exitCode = process.waitFor()

            if (exitCode != 0 || !jpgFile.exists()) {
                failures("FFmpeg failed for $fileNameWithoutExt")
            }

            // remove temporary AV1 file
            av1File.delete()
            failures("Exported ${jpgFile.name}")

        } catch (e: Exception) {

            when (e) {
                is IOException -> {
                    failures(
                        "FFmpeg not available or cannot be executed: ${e.message}"
                    )
                }

                else -> {
                    failures(
                        "Failed exporting $fileNameWithoutExt: ${e.message}"
                    )
                }
            }
        }
    }
}