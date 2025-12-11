package com.moe.twitter.data.remote.auth

/**
 * Interface for securely storing and retrieving OAuth tokens.
 */
interface TokenStorage {
    /**
     * Stores the access token.
     */
    fun saveAccessToken(token: String)

    /**
     * Retrieves the stored access token.
     *
     * @return The access token, or null if not available
     */
    fun getAccessToken(): String?

    /**
     * Stores the refresh token.
     */
    fun saveRefreshToken(token: String)

    /**
     * Retrieves the stored refresh token.
     *
     * @return The refresh token, or null if not available
     */
    fun getRefreshToken(): String?

    /**
     * Stores the PKCE code verifier for later verification.
     */
    fun saveCodeVerifier(codeVerifier: String)

    /**
     * Retrieves the stored code verifier.
     *
     * @return The code verifier, or null if not available
     */
    fun getCodeVerifier(): String?

    /**
     * Stores the OAuth state parameter for validation.
     */
    fun saveState(state: String)

    /**
     * Retrieves the stored OAuth state parameter.
     */
    fun getState(): String?

    /**
     * Clears only transient auth artifacts (code verifier/state).
     */
    fun clearEphemeralAuth()

    /**
     * Clears all stored tokens.
     */
    fun clearTokens()
}
