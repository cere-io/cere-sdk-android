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
import io.cere.cere_sdk.handlers.PageLoadingListener
import io.cere.cere_sdk.models.Event
import io.cere.cere_sdk.models.PredefinedEventType
import io.cere.cere_sdk.models.init.AuthType
import io.cere.cere_sdk.models.init.InitConfig
import io.cere.cere_sdk.models.init.InitStatus

/**
 * This is the main class which incapsulates all logic (opening/closing activity etc) and
 * provides high-level methods to manipulate with.
 **
 * <p>All you need to start working with the class is to instantiate <tt>CereModule</tt> once and
 * initialize it with call @see[init] method.
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

    internal var pageLoadingListener: PageLoadingListener? = null

    lateinit var webview: WebView

    var initStatus: InitStatus = InitStatus.Uninitialised
        private set

    private val backEventsList = mutableListOf<Event>()
    private var potentialBackEvent: Event? = null
    private var webViewStartedCallback: (() -> Unit)? = null

    /**
     * Initializes and prepares the SDK for usage.
     */
    @Throws(IllegalArgumentException::class)
    fun init(initConfig: InitConfig) {
        backEventsList.clear()
        potentialBackEvent = null
        StringBuilder(initConfig.baseUrl)
            .apply {
                append("?appId=")
                append(initConfig.appId)
                append("&integrationPartnerUserId=")
                append(initConfig.integrationPartnerUserId)
                append("&platform=android")
                append("&version=")
                append(BuildConfig.VERSION_NAME)
                append("&env=")
                append(initConfig.environment)
                append("&type=")
                append(initConfig.authType.name)
                when (initConfig.authType) {
                    AuthType.EMAIL -> {
                        validateRequiredField(
                            initConfig.email,
                            "email",
                            initConfig.authType
                        ) {
                            append("&email=")
                            append(it)
                        }
                        validateRequiredField(
                            initConfig.password,
                            "password",
                            initConfig.authType
                        ) {
                            append("&password=")
                            append(it)
                        }
                    }
                    else -> {
                        validateRequiredField(
                            initConfig.accessToken,
                            "accessToken",
                            initConfig.authType
                        ) {
                            append("&accessToken=")
                            append(it)
                        }
                    }
                }
            }
            .toString()
            .let { url ->
                Log.i(TAG, "load url $url")
                initStatus = InitStatus.Initialising
                webview.loadUrl(url)
            }
    }

    @Throws(IllegalArgumentException::class)
    private fun validateRequiredField(
        fieldValue: String?,
        fieldName: String,
        authType: AuthType,
        successFunc: (field: String) -> Unit
    ) {
        fieldValue
            ?.takeIf { it.isNotBlank() }
            ?.let { successFunc(it) }
            ?: throw IllegalArgumentException("$fieldName is null or blank. $fieldName is required for $authType")
    }

    @JavascriptInterface
    fun engagementReceived() {
        Log.i(TAG, "engagement received on android")
        Intent(context, WebviewActivity::class.java)
            .apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            .let { context.startActivity(it) }
        webViewStartedCallback?.invoke()
        webViewStartedCallback = null
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
        val receivedEvent = Event(event, payload)
        if (onEventReceivedHandler?.handle(receivedEvent) != false) {
            handleReceivedEvent(receivedEvent)
        }
    }

    /**
     * Send event to RXB for start working.
     *
     * @param event [Event]
     */
    fun sendEventForStart(event: Event, successFunc: () -> Unit) {
        webViewStartedCallback = successFunc
        sendEvent(event)
    }

    /**
     * Send event to RXB.
     *
     * @param event [Event]
     */
    fun sendEvent(event: Event) {
        handleReceivedEvent(event)
    }

    fun onBackPressed(): Boolean {
        val backPressedSuccess = backEventsList.size > 1
        sendEvent(Event(PredefinedEventType.NAVIGATE_PREVIOUS_PAGE.name, "{}"))
        return backPressedSuccess
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

    private fun handleReceivedEvent(event: Event) {
        when (PredefinedEventType.byEventType(event.eventType)) {
            PredefinedEventType.PAGE_LOADED -> {
                potentialBackEvent?.let { backEventsList.add(it) }
                potentialBackEvent = null
                Handler(Looper.getMainLooper())
                    .post {
                        pageLoadingListener?.hideLoading()
                    }
            }
            PredefinedEventType.NAVIGATE_PREVIOUS_PAGE -> {
                backEventsList.takeIf { it.size > 1 }
                    ?.run {
                        removeLastOrNull()
                        lastOrNull()?.let { sendEventToRXB(it) }
                    }
                    ?: backEventsList.clear()
            }
            PredefinedEventType.USER_LOGOUT ->
                Handler(Looper.getMainLooper())
                    .post { logout() }
            else -> {
                potentialBackEvent = event
                sendEventToRXB(event)
            }
        }
    }

    private fun logout() {
        webview.run {
            clearHistory()
            clearFormData()
            clearSslPreferences()
        }
        onEventReceivedHandler = null
        onInitializationErrorHandler = null
        onInitializationFinishedHandler = null
        backEventsList.clear()
        potentialBackEvent = null
        pageLoadingListener = null
        initStatus = InitStatus.Uninitialised
        instance = null
    }

    /**
     * Send event to RXB.
     *
     * @param event [Event]
     */
    private fun sendEventToRXB(event: Event) {
        event.takeIf { initStatus == InitStatus.Initialised }?.run {
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
                    pageLoadingListener?.showLoading()
                    Log.i(TAG, "evaluate send event javascript")
                    webview.evaluateJavascript(script) {
                        Log.i(TAG, "send event $eventType executed")
                    }
                }
        }
    }
}