package com.horsesgo.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIColor
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKScriptMessage
import platform.WebKit.WKScriptMessageHandlerProtocol
import platform.WebKit.WKUserContentController
import platform.WebKit.WKUserScript
import platform.WebKit.WKUserScriptInjectionTime
import platform.WebKit.WKWebView
import platform.darwin.NSObject

private const val CHANNEL = "horsesGo"

@OptIn(ExperimentalForeignApi::class)
private class BoardPage(private val html: String, private val onSquare: (String) -> Unit) :
    NSObject(), WKNavigationDelegateProtocol, WKScriptMessageHandlerProtocol {
    private var ready = false
    private var latestPosition: String? = null

    fun update(view: WKWebView, script: String) {
        if (latestPosition == script) return
        latestPosition = script
        if (ready) view.evaluateJavaScript(script, completionHandler = null)
    }

    override fun userContentController(
        userContentController: WKUserContentController,
        didReceiveScriptMessage: WKScriptMessage,
    ) {
        if (!didReceiveScriptMessage.frameInfo.mainFrame) return
        val message = didReceiveScriptMessage.body as? String ?: return
        if (message == "ready") {
            ready = true
            latestPosition?.let { didReceiveScriptMessage.webView?.evaluateJavaScript(it, completionHandler = null) }
        } else {
            onSquare(message)
        }
    }

    override fun webViewWebContentProcessDidTerminate(webView: WKWebView) {
        ready = false
        webView.loadHTMLString(html, baseURL = null)
    }
}

@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@Composable
internal actual fun ChessgroundView(html: String, script: String, modifier: Modifier, onSquare: (String) -> Unit) {
    val currentOnSquare by rememberUpdatedState(onSquare)
    val page = remember(html) { BoardPage(html) { currentOnSquare(it) } }
    UIKitView(
        modifier = modifier,
        properties = UIKitInteropProperties(isNativeAccessibilityEnabled = true),
        factory = {
            WKWebView().apply {
                opaque = false
                backgroundColor = UIColor.clearColor
                scrollView.scrollEnabled = false
                navigationDelegate = page
                configuration.userContentController.apply {
                    addScriptMessageHandler(page, name = CHANNEL)
                    // The load event waits for the inline Chessground module to finish.
                    addUserScript(WKUserScript(
                        source = "window.addEventListener('load', () => window.webkit.messageHandlers.horsesGo.postMessage('ready'), {once: true});",
                        injectionTime = WKUserScriptInjectionTime.WKUserScriptInjectionTimeAtDocumentStart,
                        forMainFrameOnly = true,
                    ))
                }
                loadHTMLString(html, baseURL = null)
            }
        },
        update = { page.update(it, script) },
        onRelease = {
            it.configuration.userContentController.removeScriptMessageHandlerForName(CHANNEL)
            it.navigationDelegate = null
            it.stopLoading()
        },
    )
}
