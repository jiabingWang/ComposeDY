package com.sixsixsix.dy

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import com.sixsixsix.dy.help.ParseHtmlCallback
import com.sixsixsix.dy.help.WebViewInterface
import com.sixsixsix.dy.help.getRealUrl
import com.sixsixsix.dy.help.getUrl
import com.sixsixsix.dy.model.ResultBean
import com.sixsixsix.dy.ui.theme.DYTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DYTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    HomeScreen()
                }
            }
        }
    }
}

@Composable
fun HomeScreen() {
    val coroutineScope = rememberCoroutineScope()
    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(
                    text = "去水印",
                    style = MaterialTheme.typography.subtitle2,
                    color = LocalContentColor.current
                )
            },
            navigationIcon = {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "返回"
                    )

                }
            }

        )
    }) { innerPadding ->
        ConstraintLayout(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
        ) {
            val (webView, textField, btnClean, btnStart) = createRefs()
            var wv: WebView? = null

            //通过mutableStateOf控制内容
            var inputLinkUrl by remember { mutableStateOf("") }

            AndroidView(factory = { ctx ->
                WebView(ctx).apply {
                    //用一个没有大小的WebView
                    layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, 1)
                }
            }, update = {
                wv = it
                initWebView(it)
            }, modifier = Modifier.constrainAs(webView) {
                top.linkTo(parent.top)
            })
            OutlinedTextField(
                value = inputLinkUrl,
                onValueChange = { inputLinkUrl = it },
                label = { Text(text = "视频链接") },
                modifier = Modifier
                    .constrainAs(textField) {
                        top.linkTo(parent.top, margin = 100.dp)
                    }
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp)
                    .height(200.dp)
                //
            )
            Button(onClick = {
                //清空输入框内容，除非状态更新
                inputLinkUrl = ""
            }, modifier = Modifier.constrainAs(btnClean) {
                top.linkTo(textField.bottom, margin = 20.dp)
                end.linkTo(textField.end, margin = 20.dp)
            }) {
                Text(text = "清空")
            }
            Button(
                onClick = { resolveLink(coroutineScope, linkUrl = inputLinkUrl, wv) },
                modifier = Modifier.constrainAs(btnStart) {
                    bottom.linkTo(parent.bottom, margin = 200.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }) {
                Text(text = "开始解析")
            }

        }
    }
}

/**
 * 初始化WebView
 */
private fun initWebView(webView: WebView) {
    webView.settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        setAppCacheEnabled(true)
        cacheMode = WebSettings.LOAD_DEFAULT
        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
    }
    webView.addJavascriptInterface(WebViewInterface(object : ParseHtmlCallback {
        override fun onResult(data: ResultBean) {
            Log.d("jiaBing", "data--${data}")
            val intent = Intent(webView.context, ResultActivity::class.java)
            intent.putExtra("data", data)
            startActivity(webView.context, intent, null)
        }


    }), "jsBridge")
    webView.webViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            return if (url.startsWith("http://") || url.startsWith("https://")) {
                view.loadUrl(url)
                true
            } else {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(webView.context, intent, null)
                    true
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }
        }

        override fun onPageStarted(view: WebView?, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
        }

        override fun onPageFinished(view: WebView, url: String) {
            if (TextUtils.equals(url, view.url)) {
                webViewStartGetHtml(view)
            }
            super.onPageFinished(view, url)
        }

        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError
        ) {
            super.onReceivedError(view, request, error)
        }
    }
}

/**
 * 注入js代码
 */
private fun webViewStartGetHtml(view: WebView) {
    Handler().postDelayed({
        view.loadUrl(
            "javascript:window.jsBridge.startGetContent('<head>'+" +
                    "document.getElementsByTagName('html')[0].innerHTML+'</head>');"
        )
        Log.d("jiaBing", "webViewStartGetHtml: ")
    }, 1000)
}

/**
 * 解析视频地址
 */
private fun resolveLink(scope: CoroutineScope, linkUrl: String, webView: WebView?) {
    scope.launch {
        withContext(Dispatchers.Default) {
            Log.d("jiaBing", "linkUrl---${linkUrl}")
            val url = getUrl(linkUrl)
            if (url == null) {
                //解析失败
            } else {
                //获得到了视频的真实地址
                val realUrl = getRealUrl(url)
                if (realUrl != null && webView != null) {
                    withContext(Dispatchers.Main) {
                        webView.loadUrl(realUrl)
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DYTheme {
        HomeScreen()
    }
}