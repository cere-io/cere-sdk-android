package io.cere.cere_sdk.handlers

import io.cere.cere_sdk.CereModule

/**
 * Interface used after @see[CereModule.init].
 * Executed after event received.
 */
interface OnEventReceivedHandler {
    /**
     * @param event [String] Event or event type
     * @param payload [String] serialized json
     *
     * @return [Boolean] Flag which means need to use realization of sdk or no
     */
    fun handle(event: String, payload: String): Boolean
}