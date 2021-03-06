package com.sixsixsix.dy.help

import android.util.Log
import android.webkit.JavascriptInterface
import com.sixsixsix.dy.model.ItemResult
import com.sixsixsix.dy.model.ResultBean
import com.sixsixsix.dy.model.Type
import org.jsoup.Jsoup
import java.io.IOException
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern

/**
 * @author : jiaBing
 * @date   : 2021/8/28
 * @desc   :
 */
/**
 * 获取视频Url
 */
fun getUrl(linkUrl: String): String? {
    val p = Pattern.compile(
        "((http|ftp|https)://)(([a-zA-Z0-9._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9&%_./-~-]*)?",
        Pattern.CASE_INSENSITIVE
    )
    val matcher = p.matcher(linkUrl)
    val find = matcher.find()
    return if (find) {
        matcher.group()
    } else {
        null
    }
}

/**
 * 截取到的需要再重定向获取真实地址
 */
fun getRealVideoUrl(url: String): String? {
    var realUrl: String? = null
    try {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.setRequestProperty(
            "user-agent", "Mozilla/5.0.html (iPhone; U; CPU iPhone OS 4_3_3 like Mac " +
                    "OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) " +
                    "Version/5.0.html.2 Mobile/8J2 Safari/6533.18.5 "
        )
        conn.instanceFollowRedirects = false
        val code = conn.responseCode
        if (code == 302) {
            realUrl = conn.getHeaderField("Location")
        }
        Log.d("jiaBing", "realUrl---${realUrl}")
        conn.disconnect()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return realUrl
}

fun getRealImgUrl(url: String): String? {
    //将第一个~开始至.image前面替换成noop
    var result: String? = null
    try {
        val startIndex = url.indexOf("~")
        val endIndex = url.indexOf(".image")
        val water = url.substring(startIndex + 1, endIndex)
        val noWater = "noop"
        result = url.replace(water, noWater)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return result
}

/**
 * 与WebView交互
 * 为了获取HTML内容
 */
class WebViewInterface(private val parseHtmlCallback: ParseHtmlCallback) {
    @JavascriptInterface
    fun startGetContent(html: String) {
        parseHtml(html)
    }

    private fun parseHtml(html: String) {
        val resultList = mutableListOf<ItemResult>()
        val document = Jsoup.parse(html)
        if (document == null) {
            //
            parseHtmlCallback.onResult(ResultBean(list = resultList))
            return
        }
        //
        val imgTag = document.getElementsByTag("img")
        imgTag.forEach {
            //以//开头的是加载动画图片地址，需要的话可以加上https:
            val imgUrl = it.attr("src")
            if (imgUrl.contains("http") || imgUrl.contains("https")) {
                getRealImgUrl(imgUrl)?.let {
                    resultList.add(ItemResult(url = it, type = Type.Img))
                }

            }
        }

        // 直接查找video标签
        val videoTag = document.getElementsByTag("video")
        if (videoTag == null) {
            parseHtmlCallback.onResult(ResultBean(list = resultList))
            return
        }
        val videoUrl = videoTag.attr("src")
        //替换视频播放地址
        val noWaterVideoUrl = videoUrl.replace("playwm", "play")
        // 获取重定向的URL
        val finalVideoUrl = getRealVideoUrl(noWaterVideoUrl)
        if (finalVideoUrl != null) {
            //还有封面链接
            resultList.add(ItemResult(url = finalVideoUrl, type = Type.Video))
        }
        parseHtmlCallback.onResult(ResultBean(list = resultList))
    }
}


interface ParseHtmlCallback {
    fun onResult(data: ResultBean)
}
