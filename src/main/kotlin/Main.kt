package org.gralenv

import blocks.askyoutubeurl.askYouTubeUrl
import blocks.getplayerresponse.vrPlayerResponse
import blocks.logo.printLogo
import extractionUtils.extractVisitorId
import extractionUtils.getStreamsByCodec
import muxer.mpfour.DashedParser
import muxer.mpfour.DashedWriter
import muxer.webm.WebMParser
import muxer.webm.WebmMuxer
import org.fusesource.jansi.AnsiConsole
import org.gralenv.utils.askItag
import org.gralenv.utils.loadConfig
import org.gralenv.utils.logStreams
import org.json.JSONArray
import org.json.JSONObject
import parsers.txt2filename
import utils.clearConsole
import utils.djDownloader
import utils.findFormatByItag
import utils.log
import utils.logUpdate
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.file.Files

fun main() {
    AnsiConsole.systemInstall()
    printLogo()
    try {
        val visitorId = extractVisitorId()
        if (visitorId==null){
            log("Failed to initialize downloader.")
            log("Exiting...")
            return
        }
        downloadVideo(visitorId)
    }catch (e: Exception){
        log("\nCLI crashed...${e.message}")
        log("Exiting...")
        return
    }


}

fun downloadVideo(visitorId: String) {
    while (true) {
        val videoId = askYouTubeUrl()

        if (videoId == "exit") {
            return
        }

        val playerResponse = vrPlayerResponse(videoId, visitorId)

        if (!playerResponse.has("streamingData")) {
            log("Streaming Data not found or video unavailable")
            log("Please try a different URL")
            continue
        }

        val title = txt2filename(
            playerResponse
                .getJSONObject("videoDetails")
                .getString("title")
        )

        log(title)

        val adaptiveFormats =
            playerResponse
                .getJSONObject("streamingData")
                .getJSONArray("adaptiveFormats")

        val avc1Streams = getStreamsByCodec(
            adaptiveFormats,
            listOf("avc1"),
            fallbackCodecs = listOf("vp9")
        )

        val vp9Streams = getStreamsByCodec(
            adaptiveFormats,
            listOf("vp9"),
            fallbackCodecs = listOf("avc1")
        )

        val av01Streams = getStreamsByCodec(
            adaptiveFormats,
            listOf("av01"),
            fallbackCodecs = listOf("webm")
        )

        val audioFmts = getStreamsByCodec(
            adaptiveFormats,
            listOf("mp4a"),
            fallbackCodecs = listOf("opus"),
            audioOnly = true
        )

        val opusStreams = getStreamsByCodec(
            adaptiveFormats,
            listOf("opus"),
            fallbackCodecs = listOf("mp4a"),
            audioOnly = true
        )

        log("Most Compatible Streams...")
        logStreams(avc1Streams, "Avc1 Streams...")
        logStreams(audioFmts, "Mp4a Streams...")

        val itag = askItag(
            showAll = {
                log("All Formats...")
                logStreams(avc1Streams, "Avc1 Streams...")
                logStreams(vp9Streams, "Vp9 Streams...")
                logStreams(av01Streams, "Av01 Streams...")
                logStreams(audioFmts, "Mp4 Audio...")
                logStreams(opusStreams, "Opus Audio Streams...")
            },
            adaptiveFormats = adaptiveFormats
        )

        log("Selected itag: $itag")
        downloadItag(adaptiveFormats,itag,title)

    }
}

fun downloadItag(adaptiveFromats: JSONArray,itag: Int,title: String){
    val audioDir = File( "audio")
    audioDir.mkdirs()

    val fmt= findFormatByItag(adaptiveFromats,itag)!!
    if (fmt.getString("mimeType").contains("audio")){
        log("Downloading Audio...")
        djDownloader(fmt.getString("url"),
            FileOutputStream("${audioDir.absolutePath}/${title}.mp3"),0L,fmt.getString("contentLength").toLong(), progress = { progress, percent, speed ->
            logUpdate("$progress ${percent}% $speed")
        })
    }else{
        if (fmt.getString("mimeType").contains("vp9")){
            val itag251=findFormatByItag(adaptiveFromats,251)!!
           downloadStream(itag251,fmt, title,true)
        }else{
            //avc1,av01
            val itag140=findFormatByItag(adaptiveFromats,140)!!
            downloadStream(itag140,fmt, title,false)
        }
    }

}

fun downloadStream(
    requiredAudioFmt: JSONObject,
    chosenFmt: JSONObject,
    title: String,
    webm: Boolean
) {
    val config = loadConfig()

    val tempDir = File("temp").also { it.mkdirs() }
    val videoDir = File("video").also { it.mkdirs() }

    try {
        when (config.mode) {
            DownloadMode.CONCAT -> downloadConcat(
                requiredAudioFmt, chosenFmt, title, webm, tempDir, videoDir
            )
            DownloadMode.SPLIT -> downloadSplit(
                requiredAudioFmt, chosenFmt, title, webm, tempDir, videoDir
            )
        }

        log("\nMerging Completed.")
        clearConsole()

    } finally {
        if (!config.keepTemp) tempDir.deleteRecursively()
    }
}

private fun downloadConcat(
    requiredAudioFmt: JSONObject,
    chosenFmt: JSONObject,
    title: String,
    webm: Boolean,
    tempDir: File,
    videoDir: File
) {
    val tempFile = File(tempDir, "streams.temp")

    FileOutputStream(tempFile).use { concated ->
        log("Downloading Video...")
        djDownloader(
            chosenFmt.getString("url"),
            concated,
            0L,
            chosenFmt.getString("contentLength").toLong()
        ) { progress, percent, speed -> logUpdate("$progress ${percent}% $speed") }

        log("\nDownloading Audio...")
        djDownloader(
            requiredAudioFmt.getString("url"),
            concated,
            0L,
            requiredAudioFmt.getString("contentLength").toLong()
        ) { progress, percent, speed -> logUpdate("$progress ${percent}% $speed") }
    }

    mergeStreams(requiredAudioFmt, chosenFmt, title, webm, tempFile, videoDir)
}

private fun downloadSplit(
    requiredAudioFmt: JSONObject,
    chosenFmt: JSONObject,
    title: String,
    webm: Boolean,
    tempDir: File,
    videoDir: File
) {
    val videoFile = File(tempDir, "video.temp")
    val audioFile = File(tempDir, "audio.temp")

    log("Downloading Video...")
    FileOutputStream(videoFile).use { out ->
        djDownloader(
            chosenFmt.getString("url"),
            out,
            0L,
            chosenFmt.getString("contentLength").toLong()
        ) { progress, percent, speed -> logUpdate("$progress ${percent}% $speed") }
    }

    log("\nDownloading Audio...")
    FileOutputStream(audioFile).use { out ->
        djDownloader(
            requiredAudioFmt.getString("url"),
            out,
            0L,
            requiredAudioFmt.getString("contentLength").toLong()
        ) { progress, percent, speed -> logUpdate("$progress ${percent}% $speed") }
    }

    // Combine split files into a single temp for uniform merging
    val tempFile = File(tempDir, "streams.temp")
    FileOutputStream(tempFile).use { out ->
        videoFile.inputStream().use { it.copyTo(out) }
        audioFile.inputStream().use { it.copyTo(out) }
    }

    mergeStreams(requiredAudioFmt, chosenFmt, title, webm, tempFile, videoDir)
}

private fun mergeStreams(
    requiredAudioFmt: JSONObject,
    chosenFmt: JSONObject,
    title: String,
    webm: Boolean,
    tempFile: File,
    videoDir: File
) {
    val videoSize = chosenFmt.getString("contentLength").toLong()
    val audioSize = requiredAudioFmt.getString("contentLength").toLong()

    RandomAccessFile(tempFile, "r").use { raf ->
        if (webm) {
            mergeWebm(raf, title, videoDir, videoSize, audioSize)
        } else {
            mergeMp4(raf, title, videoDir, videoSize, audioSize)
        }
    }
}

private fun mergeWebm(
    raf: RandomAccessFile,
    title: String,
    videoDir: File,
    videoSize: Long,
    audioSize: Long
) {
    val videoParser = WebMParser(raf, false, 0L, videoSize)
    val audioParser = WebMParser(raf, false, videoSize, videoSize + audioSize)

    videoParser.parse()
    audioParser.parse()

    WebmMuxer(
        File("${videoDir.absolutePath}/$title.webm"),
        listOf(videoParser, audioParser)
    ) { samples, percent -> logUpdate("$samples $percent%") }
        .writeSegment()
}

private fun mergeMp4(
    raf: RandomAccessFile,
    title: String,
    videoDir: File,
    videoSize: Long,
    audioSize: Long
) {
    val videoParser = DashedParser(raf, false, 0, videoSize)
    val audioParser = DashedParser(raf, false, videoSize, videoSize + audioSize)

    val totalSamples = videoParser.trunEntries + audioParser.trunEntries
    var samplesWritten = 0

    RandomAccessFile(File("${videoDir.absolutePath}/$title.mp4"), "rw").use { outRaf ->
        DashedWriter(outRaf, 0, mutableListOf(videoParser, audioParser)) {
            samplesWritten++
            if (samplesWritten % 2000 == 0) {
                val percent = (samplesWritten * 100) / totalSamples
                logUpdate("Merging-$samplesWritten/$totalSamples $percent%")
            }
        }.buildNonFmp4()
    }
}


