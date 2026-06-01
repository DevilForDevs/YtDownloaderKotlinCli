package org.gralenv.utils

import org.json.JSONObject
import utils.log

fun logStreams(list: List<JSONObject>, title: String) {
    log(title)

    for (k in list) {
        val itag = k.optInt("itag")

        val resolution = k.optString("height").takeIf { it.isNotBlank() }
        val size = k.optString("size").takeIf { it.isNotBlank() }
        val bitrate = k.optString("bitrate").takeIf { it.isNotBlank() }

        val line = when {
            resolution != null -> "$itag  ${resolution}p  ${size ?: ""}"
            bitrate != null -> "$itag  ${bitrate}  ${size ?: ""}"
            else -> "$itag  ${size ?: ""}"
        }

        log(line.trim())
    }
}
