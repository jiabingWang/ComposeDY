package com.sixsixsix.dy.help

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * 下载视频，监听下载完成的广播
 */
class DownLoadCompleteReceive : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == intent?.action) {
            //从广播中取出下载的id
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            val query = DownloadManager.Query()
            val downloadManager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            query.setFilterById(id)
            val cursor = downloadManager.query(query)
            if (cursor != null) {
                Log.d("jiaBing", "onReceive: $id ")
            }
        }
    }
}