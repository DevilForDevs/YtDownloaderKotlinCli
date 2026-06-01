package extractionUtils
import okhttp3.OkHttpClient
import okhttp3.Request

fun extractVisitorId(): String? {
    val client = OkHttpClient()

    val request = Request.Builder()
        .url("https://www.youtube.com")
        .header(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/137.0.0.0 Safari/537.36"
        )
        .build()

    client.newCall(request).execute().use { response ->
        val html = response.body?.string()
            ?: throw RuntimeException("Empty response")

        val visitorData = Regex("\"VISITOR_DATA\":\"([^\"]+)\"")
            .find(html)
            ?.groupValues?.get(1)

        val clientVersion = Regex("\"INNERTUBE_CLIENT_VERSION\":\"([^\"]+)\"")
            .find(html)
            ?.groupValues?.get(1)

        val apiKey = Regex("\"INNERTUBE_API_KEY\":\"([^\"]+)\"")
            .find(html)
            ?.groupValues?.get(1)

        return visitorData
    }
}