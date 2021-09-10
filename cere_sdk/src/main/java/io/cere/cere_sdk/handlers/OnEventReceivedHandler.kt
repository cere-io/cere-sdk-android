package io.cere.cere_sdk.handlers

import io.cere.cere_sdk.CereModule
import io.cere.cere_sdk.models.Event

/**
 * Interface used after @see[CereModule.init].
 * Executed after event received.
 */
interface OnEventReceivedHandler {
    /**
     * @param event [Event]
     *
     * @return [Boolean] Flag which means need to use realization of sdk or no
     */
    fun handle(event: Event): Boolean
}