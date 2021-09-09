package io.cere.cere_sdk

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieCompositionFactory
import io.cere.cere_sdk.handlers.PageLoadingListener
import io.cere.cere_sdk.utils.AnimationHelper

class WebviewActivity : AppCompatActivity(), PageLoadingListener {

    private lateinit var webview: WebView
    private var lawProgress: LottieAnimationView? = null
    private val animationHelper = AnimationHelper()

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
        LottieCompositionFactory.clearCache(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setFinishOnTouchOutside(true)
        setContentView(R.layout.activity_webview)
        CereModule.getInstance(application).run {
            attachBridgeView(webview)
            pageLoadingListener = this@WebviewActivity
        }
        lawProgress = findViewById(R.id.lawProgress)
    }

    override fun onDestroy() {
        animationHelper?.stopAnimation()
        detachBridgeView()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (CereModule.getInstance(application).onBackPressed().not()) {
            super.onBackPressed()
        }
    }

    override fun showLoading() {
        lawProgress?.visibility = View.VISIBLE
        animationHelper.startTimingEngineOfFloat(durationMs = 2000) {
            lawProgress?.progress = it
        }
    }

    override fun hideLoading() {
        lawProgress?.visibility = View.GONE
        animationHelper.stopAnimation()
    }

    private fun attachBridgeView(wv: WebView) {
        webview = wv
        findViewById<FrameLayout>(R.id.flContent)?.addView(
            webview,
            ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        )
    }

    private fun detachBridgeView() {
        (webview.parent as? ViewGroup)?.removeAllViews()
    }
}