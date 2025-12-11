package com.moe.twitter.data.remote.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.moe.twitter.BuildConfig

/**
 * Manages Twitter OAuth 2.0 authentication flow with PKCE.
 */
class OAuthManager(
    private val context: Context,
    private val tokenStorage: TokenStorage
) {

    /**
     * Initiates the OAuth 2.0 authorization flow.
     *
     * Generates PKCE parameters, stores the code verifier, and launches
     * the Twitter authorization URL in a browser.
     *
     * @return The code verifier that should be stored for later verification
     */
    fun startAuthFlow(): String {
        // Generate PKCE parameters
        val codeVerifier = PKCEGenerator.generateCodeVerifier()
        val codeChallenge = PKCEGenerator.generateCodeChallenge(codeVerifier)

        // Store code verifier for later use during token exchange
        tokenStorage.saveCodeVerifier(codeVerifier)

        // Build authorization URL
        val authUrl = buildAuthorizationUrl(
            clientId = BuildConfig.TWITTER_CLIENT_ID,
            redirectUri = BuildConfig.TWITTER_REDIRECT_URI,
            codeChallenge = codeChallenge
        )

        // Launch browser with authorization URL
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)

        return codeVerifier
    }

    /**
     * Builds the Twitter OAuth 2.0 authorization URL with PKCE.
     */
    private fun buildAuthorizationUrl(
        clientId: String,
        redirectUri: String,
        codeChallenge: String
    ): String {
        return Uri.Builder()
            .scheme("https")
            .authority("twitter.com")
            .appendPath("i")
            .appendPath("oauth2")
            .appendPath("authorize")
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("client_id", clientId)
            .appendQueryParameter("redirect_uri", redirectUri)
            .appendQueryParameter("scope", "tweet.read tweet.write users.read offline.access")
            .appendQueryParameter("state", generateRandomState())
            .appendQueryParameter("code_challenge", codeChallenge)
            .appendQueryParameter("code_challenge_method", "S256")
            .build()
            .toString()
    }

    /**
     * Generates a random state parameter for CSRF protection.
     */
    private fun generateRandomState(): String {
        return PKCEGenerator.generateCodeVerifier()
    }

    /**
     * Checks if the user is currently authenticated.
     */
    fun isAuthenticated(): Boolean {
        return tokenStorage.getAccessToken() != null
    }

    /**
     * Logs out the user by clearing all stored tokens.
     */
    fun logout() {
        tokenStorage.clearTokens()
    }
}
