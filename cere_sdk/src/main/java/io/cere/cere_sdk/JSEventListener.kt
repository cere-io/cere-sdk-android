package io.cere.cere_sdk

import android.app.Activity

interface JSEventListener {

    fun onJSEventReceived(eventName: String, activity: Activity)
}