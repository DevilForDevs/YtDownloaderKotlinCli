package org.gralenv.utils

import org.json.JSONArray
import utils.log

fun askItag(
    showAll: () -> Unit,
    adaptiveFormats: JSONArray
): Int {
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

        val itag = input.toIntOrNull()

        if (itag == null) {
            log("Please enter a valid itag number.")
            continue
        }

        val exists = (0 until adaptiveFormats.length()).any { i ->
            adaptiveFormats.getJSONObject(i)
                .optInt("itag", -1) == itag
        }

        if (exists) {
            return itag
        }

        log("Itag $itag not found. Type 'all' to see all formats.")
    }
}
