package blocks.askyoutubeurl

import extractionUtils.extractVideoId
import utils.log
import java.util.Locale.getDefault

fun askYouTubeUrl(): String {
    log("Enter YouTube Url")
    log(">")
    val  url=readln()
    if (url.lowercase(getDefault()) =="q"){
        return "exit"
    }
    val videoId= extractVideoId(url)
    if (videoId==null){
        log("Invalid Url >")
        askYouTubeUrl()
    }else{
        return videoId
    }


    return ""
}