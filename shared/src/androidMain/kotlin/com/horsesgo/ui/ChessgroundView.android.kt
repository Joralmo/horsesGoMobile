package com.horsesgo.ui

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
internal actual fun ChessgroundView(html: String, script: String, modifier: Modifier, onSquare: (String) -> Unit) {
    val currentOnSquare by rememberUpdatedState(onSquare)
    var ready by remember { mutableStateOf(false) }
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                settings.javaScriptEnabled = true
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                settings.blockNetworkLoads = true
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String?) {
                        ready = true
                    }

                    @Deprecated("Also handles requests from Android 6")
                    override fun shouldOverrideUrlLoading(view: WebView, url: String?) = true
                }
                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun play(square: String) {
                        post { if (isAttachedToWindow) currentOnSquare(square) }
                    }
                }, "HorsesGo")
                loadDataWithBaseURL("https://horses-go.invalid/", html, "text/html", "UTF-8", null)
            }
        },
        update = { if (ready) it.evaluateJavascript(script, null) },
        onRelease = {
            it.removeJavascriptInterface("HorsesGo")
            it.destroy()
        },
    )
}
