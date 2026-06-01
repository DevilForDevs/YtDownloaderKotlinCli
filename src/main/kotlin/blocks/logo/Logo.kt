package blocks.logo

import org.fusesource.jansi.Ansi.ansi

fun printLogo() {

    println()
    println(ansi().fgRgb(11, 217, 4).bold().a("__   _______ ____    ____ _     ___").reset())
    println(ansi().fgRgb(11, 217, 4).bold().a("\\ \\ / /_   _|  _ \\  / ___| |   |_ _|").reset())
    println(ansi().fgRgb(11, 217, 4).bold().a(" \\ V /  | | | | | | | |   | |    | |").reset())
    println(ansi().fgRgb(11, 217, 4).bold().a("  | |   | | | |_| | | |___| |___ | |").reset())
    println(ansi().fgRgb(11, 217, 4).bold().a("  |_|   |_| |____/  \\____|_____|___|").reset())

    println(ansi().fgRgb(11, 217, 4).bold().a("==============================================================").reset())
    println(ansi().fgRgb(11, 217, 4).bold().a("               YouTube Downloader CLI").reset())
    println(ansi().fgRgb(11, 217, 4).bold().a("Press Q to exit").reset())
    println()
}