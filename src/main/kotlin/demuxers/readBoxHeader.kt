package demuxers

import java.io.RandomAccessFile


private fun logInvalid(
    message: String,
    offset: Long,
    type: String = "",
    size: Long = 0
) {

    val caller = Thread.currentThread().stackTrace.firstOrNull {
        it.methodName != "readBoxHeader" &&
                it.className.contains("Parser")
    }

    val where = caller?.let {
        "${it.methodName}() [${it.fileName}:${it.lineNumber}]"
    } ?: "unknown"

    println("⚠️ $message (type=$type size=$size offset=$offset) — $where")
}

fun readBoxHeader(reader: RandomAccessFile): BoxHeader? {

    if (reader.filePointer + 8 > reader.length()) {
        return null
    }

    val boxOffset = reader.filePointer

    var boxSize = reader.readInt().toLong() and 0xFFFFFFFFL

    val type = ByteArray(4)
        .also(reader::readFully)
        .toString(Charsets.US_ASCII)

    val headerSize = when (boxSize) {
        1L -> {
            if (reader.filePointer + 8 > reader.length()) {
                logInvalid("Corrupt extended-size box", boxOffset)
                return null
            }

            boxSize = reader.readLong()

            16L
        }

        0L -> reader.length() - boxOffset

        else -> 8L
    }

    if (boxSize < headerSize || boxOffset + boxSize > reader.length()) {
        logInvalid("Invalid box", boxOffset, type, boxSize)
        return null
    }

    return BoxHeader(
        type = type,
        size = boxSize,
        offset = boxOffset,
        payloadOffset = reader.filePointer,
        payloadSize = boxSize - headerSize
    )
}