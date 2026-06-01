package extractionUtils

fun extractVideoId(url: String): String? {
    val regex = """^.*(?:(?:youtu\.be\/|v\/|vi\/|u\/\w\/|embed\/|shorts\/|live\/)|(?:(?:watch)?\?v(?:i)?=|\&v(?:i)?=))([^#\&\?]*).*""".toRegex()
    val matchResult = regex.find(url)
    if (matchResult != null) {
        val videoId = matchResult.groupValues[1]
        return videoId
    }
    return null
}