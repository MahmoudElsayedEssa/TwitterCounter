package com.moe.twitter.data.remote.auth

/**
 * Provides Twitter OAuth 2.0 Bearer tokens for API requests.
 */
interface TwitterTokenProvider {
    /**
     * Returns the current access token, or null if not authenticated.
     */
    fun getToken(): String?
}
