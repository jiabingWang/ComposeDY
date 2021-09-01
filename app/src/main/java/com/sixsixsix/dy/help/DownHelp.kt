package com.sixsixsix.dy.help
import android.content.Context
import java.text.DecimalFormat


object DownHelp {
    const val DOWNLOAD_URL: String = "download_url"

    const val DOWNLOAD_FILENAME: String = "download_filename"
    const val DIR_VIDEO_FILE: String = "/Download"
    fun downVideo(videoUrl: String, context: Context) {

    }
    /**
     * 保留0-2位小数
     */
    fun getFormatPrice(price: Double): String {
        var retValue: String? = null
        val df = DecimalFormat()
        df.minimumFractionDigits = 0
        df.maximumFractionDigits = 2
        retValue = df.format(price)
        retValue = retValue!!.replace(",".toRegex(), "")
        return retValue
    }

}