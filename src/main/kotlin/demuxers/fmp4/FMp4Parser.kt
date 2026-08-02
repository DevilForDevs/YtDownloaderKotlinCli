package demuxers.fmp4

import demuxers.models.Ftyp
import demuxers.HandlerType
import demuxers.TrakFilter
import demuxers.models.Moof
import demuxers.models.Moov
import demuxers.readBoxHeader
import java.io.RandomAccessFile

class FMp4Parser(
    val reader: RandomAccessFile,
    val doLogging: Boolean,
    val startOffset: Long,
    val endOffset: Long,
    val trakFilter: TrakFilter?=null
){

    var ftyp: Ftyp?=null
    var moov: Moov?=null

    fun parse() {

        reader.seek(startOffset)
        while (reader.filePointer < endOffset) {
            val header = readBoxHeader(reader) ?: break
            if (doLogging){
                println("Box ${header.type} Size ${convertBytes(header.size)} Offset ${header.offset}")
            }
            when (header.type) {

                "ftyp" -> {
                    ftyp= Ftyp.getBox(header,reader,doLogging)
                }
                "moov"->{
                    moov = Moov.getBox(
                        header,
                        reader,
                        doLogging,
                        trakFilter,
                        endOffset,
                        startOffset
                    )
                    moov?.let {
                        if (trakFilter!=null){
                            if (it.traks.isEmpty()){
                                break
                            }
                        }
                    }
                }
                "moof"->{
                    val moof=Moof.getBox(header,reader, moov?.traks)
                }

                else -> {
                    // Unknown box
                }
            }

            reader.seek(header.offset + header.size)
        }
    }

    fun convertBytes(sizeInBytes: Long): String {
        val kilobyte = 1024
        val megabyte = kilobyte * 1024
        val gigabyte = megabyte * 1024

        return when {
            sizeInBytes >= gigabyte -> String.format("%.2f GB", sizeInBytes.toDouble() / gigabyte)
            sizeInBytes >= megabyte -> String.format("%.2f MB", sizeInBytes.toDouble() / megabyte)
            sizeInBytes >= kilobyte -> String.format("%.2f KB", sizeInBytes.toDouble() / kilobyte)
            else -> "$sizeInBytes Bytes"
        }
    }

}