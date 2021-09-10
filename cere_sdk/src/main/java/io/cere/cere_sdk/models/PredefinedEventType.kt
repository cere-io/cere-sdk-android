package io.cere.cere_sdk.models

enum class PredefinedEventType {
    PAGE_LOADED,
    NAVIGATE_PREVIOUS_PAGE,
    USER_LOGOUT;

    companion object {
        fun byEventType(eventType: String) = values().find { it.name == eventType }
    }
}