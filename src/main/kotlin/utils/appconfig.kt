package org.gralenv.utils

import org.gralenv.AppConfig
import org.gralenv.DownloadMode
import java.io.File
import java.util.Properties

fun loadConfig(): AppConfig {
    val props = Properties()

    val file = File("config.properties")

    if (file.exists()) {
        file.inputStream().use(props::load)
    }

    return AppConfig(
        mode = runCatching {
            DownloadMode.valueOf(
                props.getProperty("download.mode", "CONCAT")
                    .uppercase()
            )
        }.getOrDefault(DownloadMode.CONCAT),

        keepTemp = props.getProperty("keep.temp", "false")
            .toBoolean()
    )
}