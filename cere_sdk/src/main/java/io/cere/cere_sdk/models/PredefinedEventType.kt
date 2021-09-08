package io.cere.cere_sdk.models

internal enum class PredefinedEventType {
    PAGE_LOADED,
    NAVIGATE_PREVIOUS_PAGE;

    companion object {
        fun byEventType(eventType: String) = values().find { it.name == eventType }
    }
}