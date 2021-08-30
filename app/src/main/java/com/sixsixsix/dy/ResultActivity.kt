package com.sixsixsix.dy

import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.google.android.exoplayer2.util.Util
import com.sixsixsix.dy.model.ItemResult
import com.sixsixsix.dy.model.ResultBean
import com.sixsixsix.dy.ui.theme.DYTheme

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val data = intent.getSerializableExtra("data")  as ResultBean
        Log.d("jiaBing", "ResultActivity--data--${data}")

        setContent {
            DYTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
//                    ResultScreen(videoUrl)
                }
            }
        }
    }
}

@Composable
fun ResultScreen(videoUrl: String) {
    Scaffold() {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
        ) {
            val (player) = createRefs()
            AndroidView(factory = { ctx ->
                PlayerView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 900)
                }
            }, update = {
                Log.d("jiaBing", "ResultScreen: videoUrl-——${videoUrl}")
                initPlayer(it.context, it, videoUrl)
            }, modifier = Modifier.constrainAs(player) {
                top.linkTo(parent.top, margin = 100.dp)
            })

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
private fun initRV(context: Context, result: List<ItemResult>) {
    LazyColumn(content = {
        items(result) { item ->
            ImageItem(item)
        }
    })
}

@Composable
private fun ImageItem(item: ItemResult) {
    Text(text = item.url)
}
