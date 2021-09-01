package com.sixsixsix.dy

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.core.content.ContextCompat.startForegroundService
import coil.compose.rememberImagePainter
import com.blankj.utilcode.util.ServiceUtils.startService
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.google.android.exoplayer2.util.Util
import com.sixsixsix.dy.ResultActivity.Companion.rvImgId
import com.sixsixsix.dy.ResultActivity.Companion.videoId
import com.sixsixsix.dy.help.DownHelp
import com.sixsixsix.dy.help.DownHelp.DOWNLOAD_FILENAME
import com.sixsixsix.dy.help.DownHelp.DOWNLOAD_URL
import com.sixsixsix.dy.help.DownLoadService
import com.sixsixsix.dy.model.ItemResult
import com.sixsixsix.dy.model.ResultBean
import com.sixsixsix.dy.model.Type
import com.sixsixsix.dy.ui.theme.DYTheme

class ResultActivity : AppCompatActivity() {
    companion object {
        //视频控件的id
        val videoId = "video"

        //图片列表控件的id
        val rvImgId = "rvImg"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val data = intent.getSerializableExtra("data") as ResultBean
        Log.d("jiaBing", "ResultActivity--data--${data}")
        //是否有视频
        var haveVideo = false
        //是否有地址
        var haveImg = false
        data.list.forEach {
            if (it.type == Type.Video) {
                haveVideo = true
            }
            if (it.type == Type.Img) {
                haveImg = true
            }
        }
        setContent {
            DYTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    ConstraintLayout(
                        constraintSet = decoupledConstraints(haveVideo),
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                    ) {
                        if (haveVideo) {
                            ResultScreen(data.list.first { it.type == Type.Video }.url,this)
                        }
                        initRV(data.list.filter { it.type == Type.Img })
                    }
                }
            }
        }
    }
}

/**
 * 约束条件
 */
private fun decoupledConstraints(haveVideo: Boolean): ConstraintSet {
    return ConstraintSet {
        val videoView = createRefFor(videoId)
        val rvImg = createRefFor(rvImgId)
        constrain(videoView) {
            top.linkTo(parent.top, margin = 50.dp)
        }
        constrain(rvImg) {
            if (haveVideo) {
                top.linkTo(videoView.bottom, margin = 50.dp)
            } else {
                top.linkTo(parent.top, margin = 50.dp)
            }
        }
    }
}

@Composable
fun ResultScreen(videoUrl: String,activity: ResultActivity) {
    Scaffold() {
        ConstraintLayout(
            modifier = Modifier
                .wrapContentWidth()
                .fillMaxWidth()
        ) {
            val (btnCopy, btnDown) = createRefs()
            AndroidView(factory = { ctx ->
                PlayerView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 900)
                }
            }, update = {

                Log.d("jiaBing", "ResultScreen: videoUrl-——${videoUrl}")
                initPlayer(it.context, it, videoUrl)
            }, modifier = Modifier.layoutId(videoId))
            Button(onClick = { /*TODO*/ }, modifier = Modifier.constrainAs(btnCopy) {
                top.linkTo(parent.bottom, margin = 10.dp)
                start.linkTo(parent.start, margin = 20.dp)
            }) {
                Text(text = "复制链接")
            }
            Button(onClick = {
                val intent =
                    Intent(activity
                        ,DownLoadService::class.java
                    ).putStringArrayListExtra(DOWNLOAD_FILENAME,
                        mutableListOf<String>("测试下载" + ".mp4") as ArrayList<String>
                    ).putStringArrayListExtra(DOWNLOAD_URL,
                        mutableListOf<String>(videoUrl) as ArrayList<String>
                    )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    activity.startForegroundService(intent)
                } else {
                    activity.startService(intent)
                }
            }, modifier = Modifier.constrainAs(btnDown) {
                top.linkTo(parent.bottom, margin = 10.dp)
                end.linkTo(parent.end, margin = 20.dp)
            }) {
                Text(text = "下载视频")
            }
        }
    }

}

private fun initPlayer(context: Context, playerView: PlayerView, videoUrl: String) {
    playerView.apply {
        useController = false
        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        val newSimpleInstance = ExoPlayerFactory.newSimpleInstance(context)
        player = newSimpleInstance.apply {
            repeatMode = Player.REPEAT_MODE_ALL
            val uri = Uri.parse(videoUrl)

            val videoSource =
                ProgressiveMediaSource.Factory(DefaultHttpDataSourceFactory("user-agent"))
                    .createMediaSource(uri)


            prepare(videoSource)
            //点击开始按钮才播放
            newSimpleInstance.playWhenReady = true
            addListener(object : Player.EventListener {
                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    super.onPlayerStateChanged(playWhenReady, playbackState)
                    Log.d(
                        "jiaBing",
                        "onPlayerStateChanged: playWhenReady--${playWhenReady}---${playbackState}--videoUrl--${videoUrl}"
                    )
                    if (playWhenReady && playbackState == Player.STATE_READY) {

                    }
                }
            })
        }
    }
}


/**
 * 功能描述
 */
@Composable
private fun initRV(result: List<ItemResult>) {
    LazyColumn(content = {
        items(result) { item ->
            ImageItem(item)
        }
    }, modifier = Modifier.layoutId(rvImgId))
}

@Composable
private fun ImageItem(item: ItemResult) {
    ConstraintLayout(
        modifier = Modifier
            .height(250.dp)
            .fillMaxWidth()
    ) {
        val (img, btnCopy, btnDown) = createRefs()
        Image(
            painter = rememberImagePainter(data = item.url),
            contentDescription = null, modifier = Modifier
                .constrainAs(img) {
                    top.linkTo(parent.top)
                }
                .fillMaxWidth()
                .height(200.dp), contentScale = ContentScale.Crop
        )
        Button(onClick = { /*TODO*/ }, modifier = Modifier.constrainAs(btnCopy) {
            top.linkTo(img.bottom, margin = 10.dp)
            start.linkTo(img.start, margin = 20.dp)
        }) {
            Text(text = "复制链接")
        }
        Button(onClick = { /*TODO*/ }, modifier = Modifier.constrainAs(btnDown) {
            top.linkTo(img.bottom, margin = 10.dp)
            end.linkTo(img.end, margin = 20.dp)
        }) {
            Text(text = "下载图片")
        }
    }

}

@Preview
@Composable
fun p() {
    ImageItem(ItemResult(url = "https://picsum.photos/300/300", Type.Img))
}