package org.gralenv.utils

import org.json.JSONArray
import utils.findFormatByItag
import utils.log

fun askItag(
    showAll: () -> Unit,
    adaptiveFormats: JSONArray
): String {
    while (true) {
        print("> ")

        val input = readLine()?.trim()

        if (input.isNullOrEmpty()) {
            log("Invalid input")
            continue
        }

        if (input.equals("all", ignoreCase = true)) {
            showAll()
            continue
        }

        // Allow resolutions like 720p, 1080p
        if (Regex("""\d+p""", RegexOption.IGNORE_CASE).matches(input)) {
            return input.lowercase()
        }

        val itag = input.toIntOrNull()

        if (itag == null) {
            log("Please enter a valid itag number or resolution (e.g. 720p).")
            continue
        }

        val exists = (0 until adaptiveFormats.length()).any { i ->
            adaptiveFormats.getJSONObject(i)
                .optInt("itag", -1) == itag
        }

        if (exists) {
            return input
        }

        log("Itag $itag not found. Type 'all' to see all formats.")
    }
}

fun playResolution(
    adaptiveFormats: JSONArray,
    resolution: String
) {
    val selectedItag= findFormatByItag(adaptiveFormats,resolution.toInt())
    println(selectedItag)
    if (selectedItag==null){
        return
    }
    if (selectedItag.getString("mimeType").contains("av")){
        val audioUrl=findFormatByItag(adaptiveFormats,140)!!.getString("url")
        ProcessBuilder(
            "mpv",
            selectedItag.getString("url"),
            "--audio-file=$audioUrl"
        )
            .inheritIO()
            .start()

        log("Playing $resolution")
    }
    if (selectedItag.getString("mimeType").contains("webm")){
        val audioUrl=findFormatByItag(adaptiveFormats,251)!!.getString("url")
        ProcessBuilder(
            "mpv",
            selectedItag.getString("url"),
            "--audio-file=$audioUrl"
        )
            .inheritIO()
            .start()

        log("Playing $resolution")
    }


}
