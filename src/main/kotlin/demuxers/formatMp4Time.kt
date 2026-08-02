package demuxers

fun formatMp4Time(seconds: Long): String {

    if (seconds == 0L) {
        return "unknown"
    }

    val mp4EpochOffset = 2082844800L // 1904 -> 1970 seconds

    val unixSeconds = seconds - mp4EpochOffset

    val date = java.time.Instant
        .ofEpochSecond(unixSeconds)
        .atZone(java.time.ZoneOffset.UTC)

    return "%02d/%02d/%04d".format(
        date.dayOfMonth,
        date.monthValue,
        date.year
    )
}

fun formatDuration(duration: Long, timescale: Long): String {

    if (duration == 0L || timescale == 0L) {
        return "unknown"
    }

    val totalSeconds = duration / timescale

    val days = totalSeconds / 86400
    val hours = (totalSeconds % 86400) / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return "%02d:%02d:%02d:%02d".format(
        days,
        hours,
        minutes,
        seconds
    )
}