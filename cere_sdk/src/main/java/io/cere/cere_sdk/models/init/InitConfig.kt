package io.cere.cere_sdk.models.init

/**
 * @param environment [String] environment type
 * @param baseUrl [String] base url to environment
 * @param appId [String] identifier of the application from RXB.
 * @param integrationPartnerUserId [String] The user’s id in the system.
 * @param authType [AuthType] Type of auth method
 * @param accessToken [String] The user’s onboarding access token in the system. Must to present for all [AuthType] except [AuthType.EMAIL].
 * @param email [String] The user’s email. Must to present for [AuthType.EMAIL].
 * @param password [String] The user’s password. Must to present for [AuthType.EMAIL].
 */
data class InitConfig(
    var environment: String,
    var baseUrl: String,
    var appId: String,
    var integrationPartnerUserId: String,
    var authType: AuthType,
    var accessToken: String?,
    var email: String? = null,
    var password: String? = null
)