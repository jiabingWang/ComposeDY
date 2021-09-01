package com.sixsixsix.dy.help

import android.app.DownloadManager
import android.database.ContentObserver
import android.database.Cursor
import android.os.Handler

/**
 *
created by qiudongdong on 2019/5/31
descrpion:
 */
class MyContentObserver : ContentObserver {

    private var handler: Handler? = null
    private var downloadManager: DownloadManager? = null
    private var downloadId: Long = 0
    private var downloadPos: Int = -1

    companion object {
        const val HANDLE_DOWNLOAD: Int = 12
    }

    constructor(handler: Handler?) : super(handler) {
        this.handler = handler
    }

    constructor(
        handler: Handler?,
        downloadManager: DownloadManager,
        downloadId: Long,
        downloadPos: Int
    ) : super(handler) {
        this.handler = handler
        this.downloadManager = downloadManager
        this.downloadId = downloadId
        this.downloadPos = downloadPos
    }

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        handler?.postDelayed({ updateProgress() }, 800)
    }

    /**
     * 发送Handler消息更新进度和状态
     */
    private fun updateProgress() {
        val bytesAndStatus = getBytesAndStatus(downloadId)
        handler?.obtainMessage(
            HANDLE_DOWNLOAD,
            bytesAndStatus[0],
            bytesAndStatus[1],
            bytesAndStatus[2]
        )?.let {
            handler?.sendMessage(
                it
            )
        }
    }

    /**
     * 通过query查询下载状态，包括已下载数据大小，总大小，下载状态
     *
     * @param downloadId
     * @return
     */
    private fun getBytesAndStatus(downloadId: Long): IntArray {
        val bytesAndStatus = intArrayOf(-1, -1, 0)
        val query = DownloadManager.Query().setFilterById(downloadId)
        var cursor: Cursor? = null
        try {
            cursor = downloadManager?.query(query)
            if (cursor != null && cursor!!.moveToFirst()) {
                //已经下载文件大小
                bytesAndStatus[0] =
                    cursor!!.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                //下载文件的总大小
                bytesAndStatus[1] = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                //下载状态
                bytesAndStatus[2] = downloadPos
            }
        } finally {
            cursor?.close()
        }
        return bytesAndStatus
    }
}