package demuxers.extractors

import demuxers.models.moov.trak.mdia.minf.stbl.Stsd
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

fun writeH264FromStsd(
    outFile: File,
    stsd: Stsd,
    idrFrameMp4: ByteArray
) {
    val avcC = stsd.avc1?.avcC
        ?: throw IllegalStateException("avcC not found")

    FileOutputStream(outFile).use { out ->

        fun start() {
            out.write(byteArrayOf(0, 0, 0, 1))
        }

        // Write SPS
        for (sps in avcC.sps) {
            start()
            out.write(sps)
        }

        // Write PPS
        for (pps in avcC.pps) {
            start()
            out.write(pps)
        }

        // Convert MP4 AVC format -> Annex B
        var offset = 0

        while (offset + 4 <= idrFrameMp4.size) {

            val size =
                ByteBuffer.wrap(idrFrameMp4, offset, 4)
                    .int

            offset += 4

            start()

            out.write(
                idrFrameMp4,
                offset,
                size
            )

            offset += size
        }
    }
}