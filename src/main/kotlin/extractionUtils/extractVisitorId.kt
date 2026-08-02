package extractionUtils
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

fun extractVisitorId(): String? {
    val client = OkHttpClient()

    val request = Request.Builder()
        .url("https://www.youtube.com")
        .header(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36"
        )
        .build()

    client.newCall(request).execute().use { response ->
        val html = response.body?.string() ?: return null

        val patterns = listOf(
            """"visitorData"\s*:\s*"([^"]+)"""",
            """"VISITOR_DATA"\s*:\s*"([^"]+)""""
        )

        for (pattern in patterns) {
            val match = Regex(pattern)
                .find(html)
                ?.groupValues
                ?.getOrNull(1)

            if (match != null) {
                return match
            }
        }

        return null
    }
}