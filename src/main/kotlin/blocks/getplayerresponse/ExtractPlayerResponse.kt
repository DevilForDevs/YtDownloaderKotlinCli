package blocks.getplayerresponse
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import utils.log

fun vrPlayerResponse(videoId: String, visitorData: String): JSONObject {
    val client = OkHttpClient()

    val url = "https://www.youtube.com/youtubei/v1/player?prettyPrint=false"
    val mediaType = "application/json".toMediaType()

    val maxAttempts = 3
    var lastException: Exception? = null

    repeat(maxAttempts) { attempt ->
        log("Attempt to get player Response $attempt")

        try {
            val jsonBody = JSONObject().apply {
                put("context", JSONObject().apply {
                    put("client", JSONObject().apply {
                        put("clientName", "ANDROID_VR")
                        put("clientVersion", "1.65.10")
                        put("deviceMake", "Oculus")
                        put("deviceModel", "Quest 3")
                        put("androidSdkVersion", 32)
                        put(
                            "userAgent",
                            "com.google.android.apps.youtube.vr.oculus/1.65.10 " +
                                    "(Linux; U; Android 12L; eureka-user Build/SQ3A.220605.009.A1) gzip"
                        )
                        put("osName", "Android")
                        put("osVersion", "12L")
                        put("hl", "en")
                        put("timeZone", "UTC")
                        put("utcOffsetMinutes", 0)
                        put("visitorData", visitorData)
                    })
                })

                put("videoId", videoId)

                put("playbackContext", JSONObject().apply {
                    put("contentPlaybackContext", JSONObject().apply {
                        put("html5Preference", "HTML5_PREF_WANTS")
                    })
                })

                put("contentCheckOk", true)
                put("racyCheckOk", true)
            }

            val body = jsonBody.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("X-YouTube-Client-Name", "28")
                .addHeader("X-YouTube-Client-Version", "1.65.10")
                .addHeader("Origin", "https://www.youtube.com")
                .addHeader(
                    "User-Agent",
                    "com.google.android.apps.youtube.vr.oculus/1.65.10 (Linux; U; Android 12L; eureka-user Build/SQ3A.220605.009.A1) gzip"
                )
                .addHeader("X-Goog-Visitor-Id", visitorData)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw RuntimeException("HTTP ${response.code}: ${response.message}")
                }

                val text = response.body?.string()
                    ?: throw RuntimeException("Empty response body")

                return JSONObject(text) // success → exit function
            }

        } catch (e: Exception) {
            lastException = e

            if (attempt < maxAttempts - 1) {
                Thread.sleep(1000L * (attempt + 1)) // simple backoff: 1s, 2s
            }
        }
    }

    throw RuntimeException("Failed after $maxAttempts attempts", lastException)
}

