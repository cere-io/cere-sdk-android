package io.cere.cere_sdk

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.*
import android.webkit.*
import io.cere.cere_sdk.handlers.OnEventReceivedHandler
import io.cere.cere_sdk.handlers.OnInitializationErrorHandler
import io.cere.cere_sdk.handlers.OnInitializationFinishedHandler

const val baseUrl: String = "https://sdk.dev.cere.io/common/native.html"

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
        private fun make(context: Context): CereModule =
            CereModule(context).configureWebView().apply {
                instance = this
            }

        @JvmStatic
        fun getInstance(application: Application): CereModule =
            instance ?: make(application.applicationContext)
    }

    var onInitializationFinishedHandler: OnInitializationFinishedHandler? = null

    var onInitializationErrorHandler: OnInitializationErrorHandler? = null

    var onEventReceivedHandler: OnEventReceivedHandler? = null

    lateinit var webview: WebView

    var initStatus: InitStatus = InitStatus.Uninitialised
        private set

    private lateinit var appId: String
    private lateinit var integrationPartnerUserId: String
    private lateinit var token: String

    private val version: String = io.cere.cere_sdk.BuildConfig.VERSION_NAME

    /**
     * Initializes and prepares the SDK for usage.
     * @param appId: identifier of the application from RXB.
     * @param integrationPartnerUserId: The user’s id in the system.
     * @param token: The user’s onboarding access token in the system.
     */
    fun init(appId: String, integrationPartnerUserId: String, token: String = "") {
        val env = BuildConfig.environment
        this.appId = appId
        this.integrationPartnerUserId = integrationPartnerUserId
        this.token = token
        val url =
            "${baseUrl}?appId=${appId}&integrationPartnerUserId=${integrationPartnerUserId}&platform=android&version=${version}&env=${env}&token=${token}"
        Log.i(TAG, "load url ${url}")
        initStatus = InitStatus.Initialising
        webview.loadUrl(url)
    }

    /**
     * Send event to RXB.
     * @param eventType: Type of event. For example `APP_LAUNCHED`.
     * @param payload: Optional parameter which can be passed with event. It should contain serialised json payload associated with eventType.
     */
    fun sendEvent(eventType: String, payload: String = "") {
        if (initStatus == InitStatus.Initialised) {
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

            Handler(Looper.getMainLooper())
                .post {
                    Log.i(TAG, "evaluate send event javascript")
                    webview.evaluateJavascript(script) {
                        Log.i(TAG, "send event $eventType executed")
                    }
                }
        }
    }

    @JavascriptInterface
    fun engagementReceived() {
        Log.i(TAG, "engagement received on android")
        Intent(context, WebviewActivity::class.java)
            .apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            .let { context.startActivity(it) }
    }

    @JavascriptInterface
    fun sdkInitialized() {
        Log.i(TAG, "sdk initialised")
        initStatus = InitStatus.Initialised
        onInitializationFinishedHandler?.handle()
    }

    @JavascriptInterface
    fun sdkInitializedError(error: String) {
        Log.i(TAG, "sdk initialise error: $error")
        initStatus = InitStatus.InitialiseError(error)
        onInitializationErrorHandler?.handle(error)
    }

    @JavascriptInterface
    fun onEventReceived(event: String, payload: String) {
        Log.i(TAG, "onEventReceived: $event, payload : $payload")
        if (onEventReceivedHandler?.handle(event, payload) != false) {
            handleReceivedEvent(event, payload)
        }
    }

    private fun configureWebView(): CereModule {
        webview = WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
            addJavascriptInterface(this@CereModule, "Android")
        }
        return this
    }

    private fun handleReceivedEvent(event: String, payload: String) {
        sendEvent(event, payload)
    }
}