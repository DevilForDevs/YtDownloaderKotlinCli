package extractionUtils

import org.json.JSONArray
import org.json.JSONObject
import parsers.convertBytes

fun getStreamsByCodec(
    adaptiveFormats: JSONArray,
    codecs: List<String>,
    fallbackCodecs: List<String>? = null,
    audioOnly: Boolean = false
): List<JSONObject> {

    fun filter(targetCodecs: List<String>): List<JSONObject> {
        val results = mutableListOf<JSONObject>()

        for (i in 0 until adaptiveFormats.length()) {
            val item = adaptiveFormats.getJSONObject(i)

            val mimeType = item.optString("mimeType", "")
            val url = item.optString("url", null) ?: continue

            val hasHeight = item.has("height") && !item.isNull("height")
            val isAudio = !hasHeight

            if (!audioOnly && isAudio) continue
            if (audioOnly && !isAudio) continue

            val matchesCodec = targetCodecs.any { codec ->
                mimeType.contains(codec, ignoreCase = true)
            }

            if (!matchesCodec) continue

            val result = JSONObject()
            result.put("url", url)
            result.put("itag", item.optInt("itag", -1))

            // 🎯 content length handling
            val contentLengthStr = item.optString("contentLength", null)
            val contentLength = contentLengthStr?.toLongOrNull() ?: -1L
            if (contentLength > 0) {
                result.put("contentLength", contentLength)
                result.put("size", convertBytes(contentLength))
            }

            if (isAudio) {
                if (item.has("bitrate")) {
                    val btr=item.getInt("bitrate")
                    result.put("bitrate", convertBytes(btr.toLong())+"/s")
                }
            } else {
                if (item.has("height")) {
                    result.put("height", item.getInt("height"))
                }
            }

            results.add(result)
        }

        return results
    }

    val primary = filter(codecs)
    if (primary.isNotEmpty()) return primary

    if (fallbackCodecs != null) {
        val fallback = filter(fallbackCodecs)
        if (fallback.isNotEmpty()) return fallback
    }

    return emptyList()
}

