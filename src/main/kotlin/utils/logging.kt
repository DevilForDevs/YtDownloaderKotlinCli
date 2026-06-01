package utils

import org.fusesource.jansi.Ansi.ansi
import org.json.JSONArray
import org.json.JSONObject

fun log(text: String){
    print(
        ansi()
            .fgRgb(11, 217, 4)
            .a(text+"\n")
            .reset()
    )
}

fun logUpdate(text: String) {
    print(
        "\r" + ansi()
            .eraseLine()
            .fgRgb(11, 217, 4)
            .a(text)
            .reset()
    )
    System.out.flush()
}

fun clearConsole() {
    print(
        ansi()
            .eraseScreen()
            .cursor(0, 0)
    )
    System.out.flush()
}

fun findFormatByItag(adaptiveFormats: JSONArray, itag: Int): JSONObject? {
    for (i in 0 until adaptiveFormats.length()) {
        val obj = adaptiveFormats.getJSONObject(i)
        if (obj.optInt("itag") == itag) {
            return obj
        }
    }
    return null
}