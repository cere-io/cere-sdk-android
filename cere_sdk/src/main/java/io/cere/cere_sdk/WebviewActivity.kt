package io.cere.cere_sdk

import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.WebView
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

class WebviewActivity : AppCompatActivity() {

    private lateinit var webview: WebView

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        val instance = CereModule.getInstance(this.application)
        instance.theme?.let { theme = it }
        super.onCreate(savedInstanceState)
        this.setFinishOnTouchOutside(true)

        attachBridgeView(instance.webview, instance.layout)
    }

    private fun attachBridgeView(wv: WebView, layoutParams: ViewGroup.LayoutParams?) {
        webview = wv
        setContentView(wv, layoutParams ?: createParams())
    }

    private fun createParams(): ViewGroup.LayoutParams {
        val params =
            RelativeLayout.LayoutParams(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
        params.width = MATCH_PARENT
        params.height = MATCH_PARENT
        return params;
    }

    private fun detachBridgeView() {
        if (webview.parent != null) {
            (webview.parent as ViewGroup).removeAllViews()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        detachBridgeView()
    }
}