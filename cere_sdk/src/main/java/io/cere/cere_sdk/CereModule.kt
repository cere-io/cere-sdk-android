package io.cere.cere_sdk

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebView
import androidx.annotation.RequiresApi
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer


const val baseUrl: String = "https://sdk.dev.cere.io/common/native.html"

/**
 * Interface used after `CereModule` init method.
 * Executed after successful initialization.
 */
interface OnInitializationFinishedHandler {
    fun handle()
}

/**
 * Interface used after `CereModule` init method.
 * Executed after initialization error.
 */
interface OnInitializationErrorHandler {
    fun handle(error: String)
}

/**
 * This is the main class which incapsulates all logic (opening/closing activity etc) and
 * provides high-level methods to manipulate with.
 **
 * <p>All you need to start working with the class is to instantiate <tt>CereModule</tt> once and
 * initialize it with 2 params. Example:
 * </p>
 *
 * <p>
 *     <pre>
 *         {@code
 *              CereModule cereModule = CereModule.getInstance(context);
 *              cereModule.init("Your appId", "Your integrationPartnerUserId");
 *         }
 *     </pre>
 * </p>
 *
 * <p>That's enough for start loading {@code CereModule}, but note that {@code CereModule} still
 * remains hidden. Also, first load of {@code CereModule} takes a some time which depends on
 * network connection quality. That's why you need to init {@code CereModule} as soon as possible.
 * </p>
 *
 * <p>If you want to show {@code CereModule} right after it has initialized, you can add listener
 * {@see OnInitializationFinishedHandler} implementation which will invoke method <tt>sendEvent</tt> on
 * {@code CereModule} instance. Example:
 * </p>
 *
 * <p>
 *     <pre>
 *         {@code
 *              cereModule.onInitializationFinishedHandler(() -> {
 *                  cereModule.sendEvent("APP_LAUNCHED_TEST", "{}");
 *              });
 *         }
 *     </pre>
 * </p>
 *
 * @author  Rudolf Markulin
 */
class CereModule(private val context: Context) {

    companion object {
        const val TAG = "CereModule"

        @Volatile
        private var instance: CereModule? = null

        @JvmStatic
        private fun make(context: Context): CereModule {
            val module = CereModule(context).configureWebView()
            instance = module
            return module
        }

        @JvmStatic
        fun getInstance(application: Application): CereModule {
            val inst = this.instance
            if (inst != null) {
                return inst
            } else {
                return make(application.applicationContext)
            }
        }
    }

    private var jsEventListenerCollection: MutableSet<JSEventListener> = HashSet()

    var onInitializationFinishedHandler: OnInitializationFinishedHandler =
        object : OnInitializationFinishedHandler {
            override fun handle() {

            }
        }

    var onInitializationErrorHandler: OnInitializationErrorHandler =
        object : OnInitializationErrorHandler {
            override fun handle(error: String) {

            }
        }

    lateinit var webview: WebView
    var layout: ViewGroup.LayoutParams? = null
    var theme: Int? = null
    var activity: Activity? = null

    private lateinit var appId: String
    private lateinit var authMethodType: String
    private lateinit var externalUserId: String
    private lateinit var integrationPartnerUserId: String
    private lateinit var token: String

    private var initStatus: InitStatus = InitStatus.Uninitialised

    private val version: String = io.cere.cere_sdk.BuildConfig.VERSION_NAME

    /**
     * @return current sdk initialization status instance of {@code InitStatus}
     */
    fun getInitStatus(): InitStatus {
        return this.initStatus
    }

    /**
     * Initializes and prepares the SDK for usage.
     * @param appId: identifier of the application from RXB.
     * @param integrationPartnerUserId: The user’s id in the system.
     * @param token: The user’s onboarding access token in the system.
     */
    fun init(
        appId: String,
        integrationPartnerUserId: String,
        token: String = "",
        externalUserId: String = "",
        authMethodType: String = "",
        env: String = "dev"
    ) {
        this.appId = appId
        this.externalUserId = externalUserId
        this.integrationPartnerUserId = integrationPartnerUserId
        this.token = token
        val url =
            "${baseUrl}?appId=${appId}&integrationPartnerUserId=${integrationPartnerUserId}&platform=android&version=${version}&env=${env}&token=${token}&externalUserId=${externalUserId}&authMethodType=${authMethodType}"
        Log.i(TAG, "load url ${url}")
        this.initStatus = InitStatus.Initialising
        this.webview.loadUrl(url)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView(): CereModule {
        val webview = WebView(context)
        webview.settings.javaScriptEnabled = true
        webview.settings.domStorageEnabled = true
        webview.settings.databaseEnabled = true
        webview.addJavascriptInterface(this, "Android")
        this.webview = webview
        return this
    }

//
//    /**
//     * Sets the padding. The view may add on the space required to display the scrollbars,
//     * depending on the style and visibility of the scrollbars. So the values returned from getPaddingLeft, getPaddingTop, getPaddingRight and getPaddingBottom may be different from the values set in this call.
//     * Params:
//     * left – the left padding in pixels
//     * top – the top padding in pixels
//     * right – the right padding in pixels
//     * bottom – the bottom padding in pixels
//     */
//    public fun setDisplay(left: Int, top: Int, right: Int, bottom: Int) {
//        this.webview?.setPadding(left, top, right, bottom)
//    }
//
//    public fun setInitialScale(scaleInPercent: Int) {
//        this.webview?.setInitialScale(scaleInPercent)
//    }
//
//    private fun getScale(): Int {
//        val display = (getSystemService(Context.WINDOW_SERVICE) as WindowManager?)!!.defaultDisplay
//        val width = display.width
//        var `val`: Double = width / PIC_WIDTH
//        `val` = `val` * 100.0
//        return `val`.toInt()
//    }

    /**
     * Send event to RXB.
     * @param eventType: Type of event. For example `APP_LAUNCHED`.
     * @param payload: Optional parameter which can be passed with event. It should contain serialised json payload associated with eventType.
     */
    fun sendEvent(eventType: String, payload: String = "") {
        if (this.initStatus == InitStatus.Initialised) {
            val script = """
                (async function() {
                    console.log('send event dialog');
                    return cereSDK.sendEvent('${eventType}', ${payload}).
                        then(() => {
                            console.log(`event ${eventType} sent`);
                        }).
                        catch(err => {
                            console.log(`${eventType} sending error` + err);
                        });
                })();""".trimIndent()

            val handler = Handler(Looper.getMainLooper())

            handler.post {
                Log.i(TAG, "evaluate send event javascript")
                webview.evaluateJavascript(script)
                {
                    Log.i(TAG, "send event $eventType executed")
                }
            }
        }
    }

    /**
     * Has nfts call.
     */
//    ASYNC countdown latch
    @RequiresApi(Build.VERSION_CODES.N)
    fun hasNfts(consumer: Consumer<String>): String {
        if (this.initStatus == InitStatus.Initialised) {

            val script = """
                (async () => {
                    return await cereSDK.hasNfts();
            })();""".trimIndent()

            val handler = Handler(Looper.getMainLooper())

            val latch = CountDownLatch(1)
            val result: AtomicReference<String> = AtomicReference()
            handler.post(Runnable {
                Log.i(TAG, "hasNfts event sent")
                webview.evaluateJavascript(script) {
                    Log.i(TAG, "has token received with $it")
                    result.set(it);
                    latch.countDown()
                }
            })
            latch.await()
            return result.get()
        }
        return "NOT_INITIALISED"
    }

//    @RequiresApi(Build.VERSION_CODES.N)
//    fun hasNfts(consumer: Consumer<String>) {
//        if (this.initStatus == InitStatus.Initialised) {
//
//            val script = """
//                (async () => {
//                    cereSDK.hasNfts();
//            })();""".trimIndent()
//
//            val handler = Handler(Looper.getMainLooper())
//            handler.post {
//                Log.i(TAG, "hasNfts event sent")
//                webview.evaluateJavascript(script) {
//                    consumer.accept(it)
//                    Log.i(TAG, "has token received with $it")
//                };
//            }
//        }
//    }

    /**
     * Send event to RXB.
     * @param eventType: Type of event for 3rd_party. For example `APP_LAUNCHED`.
     */
    fun sendTrustedEvent(eventType: String) {
        if (this.initStatus == InitStatus.Initialised) {
            val script = """
                (async function() {
                let timestamp = Number(new Date());
                let signature = await cereSDK.signMessage(timestamp);
                let payload = {
                    timestamp,
                    signature
                };
                console.log(JSON.stringify(payload))
                cereSDK.sendEvent('${eventType}', payload);
            })();""".trimIndent()

            val handler = Handler(Looper.getMainLooper())

            handler.post {
                Log.i(TAG, "evaluate send event javascript")
                webview.evaluateJavascript(script)
                {
                    Log.i(TAG, "send event $eventType executed")
                }
            }
        }
    }

    fun attachEventListener(jsEventListener: JSEventListener) =
        jsEventListenerCollection.add(jsEventListener)

    fun detachEventListener(jsEventListener: JSEventListener) =
        jsEventListenerCollection.remove(jsEventListener)

    private fun notifyListeners(eventName: String, activity: Activity?) =
        activity?.let { act ->
            jsEventListenerCollection.forEach { it.onJSEventReceived(eventName, act) }
        }

    @JavascriptInterface
    fun engagementReceived() {
        Log.i(TAG, "engagement received on android")
        val intent = Intent(context, WebviewActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    @JavascriptInterface
    fun sdkInitialized() {
        Log.i(TAG, "sdk initialised")
        this.initStatus = InitStatus.Initialised
        onInitializationFinishedHandler.handle()
    }

    @JavascriptInterface
    fun sdkInitializedError(error: String) {
        Log.i(TAG, "sdk initialise error: $error")
        this.initStatus = InitStatus.InitialiseError(error)
        onInitializationErrorHandler.handle(error)
    }

    @JavascriptInterface
    fun onJSAction(eventName: String) {
        Log.i(TAG, "Event received $eventName")
        notifyListeners(eventName, activity)
    }

}