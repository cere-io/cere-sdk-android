package io.cere.cere_sdk.models.init

sealed class InitStatus {
    object Uninitialised : InitStatus()
    object Initialising : InitStatus()
    object Initialised : InitStatus()
    data class InitialiseError(@JvmField val error: String) : InitStatus()
}