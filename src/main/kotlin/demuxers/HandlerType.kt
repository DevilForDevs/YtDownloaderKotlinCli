package demuxers

enum class HandlerType(val code: String) {
    VIDEO("vide"),
    AUDIO("soun"),
    SUBTITLE("sbtl"),
    METADATA("meta"),
    TIMECODE("tmcd"),
    HINT("hint"),
    UNKNOWN("")
}