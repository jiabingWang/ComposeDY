package com.sixsixsix.dy.help

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.sixsixsix.dy.R
import com.sixsixsix.dy.help.DownHelp.getFormatPrice
import java.io.File
import java.text.DecimalFormat

/**
 * 下载服务
 */
class DownLoadService :Service() {
    internal lateinit var request: DownloadManager.Request
    private lateinit var myServiceBind: MyServiceBinder
    private lateinit var downLoadManger: DownloadManager
    private var isRegisterReceive = false
    private var myContentObserver: MyContentObserver? = null
    private lateinit var myDownLoadCompleteReceive: DownLoadCompleteReceive
    private var arrayList = arrayListOf<String>()

    companion object {
        var hanlder: Handler = @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                if (msg.what == MyContentObserver.HANDLE_DOWNLOAD) {
                    //被除数可以为0，除数必须大于0
                    if (msg.arg1 >= 0 && msg.arg2 > 0) {
                        val downloadPercent = getFormatPrice((msg.arg1 / msg.arg2.toDouble()))
                        Log.d("jiaBing", "handleMessage: 当前下载进度${ (downloadPercent.toDouble() * 100).toInt()}--${msg.obj as Int}")
                    }
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        myServiceBind = MyServiceBinder()
        return myServiceBind
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val downLoadUrl =
            intent?.getStringArrayListExtra(DownHelp.DOWNLOAD_URL)
                ?: mutableListOf<String>()
        val downLoadFileName =
            intent?.getStringArrayListExtra(DownHelp.DOWNLOAD_FILENAME)
                ?: mutableListOf<String>()
        if (downLoadUrl.isNotEmpty()) {
            dowloadVideo(downLoadUrl[0], downLoadFileName[0])
        }
        return START_STICKY_COMPATIBILITY
    }
    inner class MyServiceBinder : Binder() {
        val service: DownLoadService
            get() = this@DownLoadService

    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                getString(R.string.download_service_channel_id),
                getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
            val notification =
                NotificationCompat.Builder(this, getString(R.string.download_service_channel_id))
                    .build()
            startForeground(100, notification)
        }
    }
    private fun dowloadVideo(url: String, fileName: String) {
        val parse = Uri.parse(url)
        request = DownloadManager.Request(parse)
        //设置下载的网络
        if (ConnectedUtils.getConnectType(this) == 0) {
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE)
        } else {
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
        }
        request.setTitle(fileName)
        request.setDescription(fileName + "正在下载...")
        //设置漫游状态下是否可以下载
        request.setAllowedOverRoaming(false)
        /**
         * 下载过程中状态栏是否可见
         */
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
        /**
         * *如果我们希望下载的文件可以被系统的Downloads应用扫描到并管理，
        我们需要调用Request对象的setVisibleInDownloadsUi方法，传递参数true.
         */
        request.setVisibleInDownloadsUi(true)
        request.allowScanningByMediaScanner()
        //Build.VERSION_CODES.Q或更高版本的应用程序，dirType必须是已知的公共目录之一，例如Environment＃DIRECTORY_DOWNLOADS
        val fileDir = File(Environment.DIRECTORY_DOWNLOADS + DownHelp.DIR_VIDEO_FILE)
        if (fileDir.isDirectory) {
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        } else {
            fileDir.mkdirs()
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        }
        downLoadManger = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        //文件下载的id
        val id = downLoadManger.enqueue(request).toString()
        arrayList.add(id)
        //取消下载的文件id
        // downLoadManger.remove(id)
        myContentObserver =
            MyContentObserver(hanlder, downLoadManger, id.toLong(), 1)
        //在执行下载前注册内容监听者
        registerContentObserver(myContentObserver)
        if (!isRegisterReceive) {
            myDownLoadCompleteReceive = DownLoadCompleteReceive()
            val intentFilter = IntentFilter()
            intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            registerReceiver(myDownLoadCompleteReceive, intentFilter)
            isRegisterReceive = true
        }
    }
    /**
     * 监听下载类
     */
    private fun registerContentObserver(myContentObserver: MyContentObserver?) {
        if (myContentObserver != null) {
            contentResolver.registerContentObserver(
                Uri.parse("content://downloads/my_downloads"), true, myContentObserver
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        myContentObserver?.let { contentResolver.unregisterContentObserver(it) }
        unregisterReceiver(myDownLoadCompleteReceive)
    }
}