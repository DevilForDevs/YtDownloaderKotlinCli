package org.gralenv

data class AppConfig(
    val mode: DownloadMode,
    val keepTemp: Boolean
)