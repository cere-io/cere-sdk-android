package io.cere.cere_sdk.handlers

import io.cere.cere_sdk.CereModule

/**
 * Interface used after @see[CereModule.init].
 * Executed after successful initialization.
 */
interface OnInitializationFinishedHandler {
    fun handle()
}