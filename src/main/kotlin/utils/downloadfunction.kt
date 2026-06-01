package utils

import okhttp3.OkHttpClient
import okhttp3.Request
import parsers.convertBytes
import parsers.convertSpeed
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

val dclient = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()

fun djDownloader(
    url: String,
    fos: FileOutputStream,
    onDisk: Long,
    totalBytes: Long,
    progress: (dbyt: String, percent: Int, speed: String) -> Unit
) {
    val chunkSize = 9_437_184L // 9 MB
    val start = onDisk
    val end = minOf(start + chunkSize - 1, totalBytes - 1)

    val request = Request.Builder()
        .url(url)
        .addHeader("Range", "bytes=$start-$end")
        .build()

    dclient.newCall(request).execute().use { response ->
        if (response.code != 206) {
            println("HTTP error: Expected 206 Partial Content, got ${response.code}")
            return
        }

        response.body?.byteStream().use { inputStream ->
            if (inputStream == null) return

            val buffer = ByteArray(1024)
            var bytesRead: Int
            var downloadedInChunk = 0L
            var speedBytes = 0L
            var lastTime = System.currentTimeMillis()

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                fos.write(buffer, 0, bytesRead)
                downloadedInChunk += bytesRead
                speedBytes += bytesRead

                val currentDownloaded = onDisk + downloadedInChunk
                val percent = ((currentDownloaded * 100) / totalBytes).toInt()
                val now = System.currentTimeMillis()

                if (now - lastTime >= 1000) {
                    val speedText = convertSpeed(speedBytes)
                    val pg = "${convertBytes(currentDownloaded)}/${convertBytes(totalBytes)}"
                    progress(pg, percent, speedText)
                    speedBytes = 0
                    lastTime = now
                }
            }

            // Final progress update
            val finalDownloaded = onDisk + downloadedInChunk
            val pg = "${convertBytes(finalDownloaded)}/${convertBytes(totalBytes)}"
            val percent = ((finalDownloaded * 100) / totalBytes).toInt()
            progress(pg, percent, convertSpeed(speedBytes))

            // Download next chunk recursively
            if (finalDownloaded < totalBytes) {
                djDownloader(url, fos, finalDownloaded, totalBytes, progress)
            }
        }
    }
}