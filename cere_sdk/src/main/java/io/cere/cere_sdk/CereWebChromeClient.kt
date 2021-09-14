package io.cere.cere_sdk

import android.app.Activity
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.WebChromeClient
import android.widget.FrameLayout

//https://www.monstertechnocodes.com/2018/07/how-to-enable-fullscreen-mode-in-any.html/
internal class CereWebChromeClient(val activity: Activity) : WebChromeClient() {

    companion object {
        private const val MAGIC_BITMAP_RESOURCE_ID = 2130837573
        private const val MAGIC_SYSTEM_UI_VISIBILITY_FLAG = 3846
    }

    private var customView: View? = null
    private var customViewCallback: CustomViewCallback? = null
    private var originalSystemUiVisibility = 0
    private var originalOrientation = 0

    override fun getDefaultVideoPoster(): Bitmap? =
        customView?.let {
            BitmapFactory.decodeResource(
                activity.applicationContext.resources,
                MAGIC_BITMAP_RESOURCE_ID
            )
        }

    override fun onHideCustomView() {
        (activity.window.decorView as? FrameLayout)?.removeView(customView)
        customView = null
        activity.window.decorView.systemUiVisibility = originalSystemUiVisibility
        activity.requestedOrientation = originalOrientation
        customViewCallback?.onCustomViewHidden()
        customViewCallback = null
    }

    override fun onShowCustomView(paramView: View, paramCustomViewCallback: CustomViewCallback) {
        customView
            ?.let { onHideCustomView() }
            ?: run {
                customView = paramView
                originalSystemUiVisibility = activity.window.decorView.systemUiVisibility
                originalOrientation = activity.requestedOrientation
                activity.requestedOrientation = SCREEN_ORIENTATION_SENSOR
                customViewCallback = paramCustomViewCallback
                (activity.window.decorView as? FrameLayout)?.addView(
                    customView,
                    FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                )
                activity.window.decorView.systemUiVisibility =
                    (MAGIC_SYSTEM_UI_VISIBILITY_FLAG or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            }
    }
}