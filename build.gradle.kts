plugins {
    kotlin("jvm") version "2.3.20"
    id("org.graalvm.buildtools.native") version "0.9.23"
}

group = "org.gralenv"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.fusesource.jansi:jansi:2.4.1")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("org.json:json:20231013")
    implementation(files("libs/muxer.jar"))
}

kotlin {

}

tasks.test {
    useJUnitPlatform()
}


graalvmNative {
    binaries {
        named("main") {
            imageName.set("YtdCli")

            buildArgs.addAll(
                "-H:Class=org.gralenv.MainKt",
                "--no-fallback",
                "--enable-url-protocols=http,https",
                "--report-unsupported-elements-at-runtime",
                "--allow-incomplete-classpath",
                "--install-exit-handlers",
                "-H:+ReportExceptionStackTraces"
            )
        }
    }
}