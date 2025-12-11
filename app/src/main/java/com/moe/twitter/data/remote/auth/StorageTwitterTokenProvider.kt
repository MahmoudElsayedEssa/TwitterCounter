package com.moe.twitter.data.remote.auth

/**
 * Provides tokens from persistent storage (SharedPreferences).
 * Used in production when user has authenticated via OAuth flow.
 */
class StorageTwitterTokenProvider(
    private val tokenStorage: TokenStorage
) : TwitterTokenProvider {
    
    override fun getToken(): String? {
        return tokenStorage.getAccessToken()
    }
}
