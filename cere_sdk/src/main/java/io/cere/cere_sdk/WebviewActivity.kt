package io.cere.cere_sdk

import android.os.Bundle
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity

class WebviewActivity : AppCompatActivity() {

    private lateinit var webview: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setFinishOnTouchOutside(true)
        attachBridgeView(CereModule.getInstance(this.application).webview)
    }

    override fun onDestroy() {
        detachBridgeView()
        super.onDestroy()
    }

    override fun onBackPressed() {
        webview.takeIf { it.canGoBack() }
            ?.goBack()
            ?: super.onBackPressed()
    }

    private fun attachBridgeView(wv: WebView) {
        webview = wv
        setContentView(webview, ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT))
    }

    private fun detachBridgeView() {
        (webview.parent as? ViewGroup)?.removeAllViews()
    }
}