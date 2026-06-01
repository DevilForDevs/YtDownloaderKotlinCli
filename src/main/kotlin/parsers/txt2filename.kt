package parsers

fun txt2filename(txt: String): String {
    val specialCharacters = listOf(
        "@", "#", "$", "*", "&", "<", ">", "/", "\\b", "|", "?", "CON", "PRN", "AUX", "NUL",
        "COM0", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT0",
        "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9", ":", "\"", "'"
    )

    var normalString = txt
    for (sc in specialCharacters) {
        normalString = normalString.replace(sc, "")
    }

    return normalString
}