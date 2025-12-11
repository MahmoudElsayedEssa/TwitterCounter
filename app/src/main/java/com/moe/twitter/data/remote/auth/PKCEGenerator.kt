package com.moe.twitter.data.remote.auth

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Generates PKCE (Proof Key for Code Exchange) parameters for OAuth 2.0 authorization.
 *
 * PKCE adds security to OAuth flows by generating a code_verifier and code_challenge
 * to prevent authorization code interception attacks.
 */
object PKCEGenerator {

    /**
     * Generates a cryptographically random code verifier.
     *
     * The code verifier is a high-entropy cryptographic random string
     * between 43-128 characters from the unreserved character set [A-Z] / [a-z] / [0-9] / "-" / "." / "_" / "~"
     *
     * @return A Base64 URL-safe encoded random string
     */
    fun generateCodeVerifier(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(
            bytes,
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )
    }

    /**
     * Generates a code challenge from the code verifier.
     *
     * The code challenge is the SHA256 hash of the code verifier, Base64 URL-safe encoded.
     * This is sent to the authorization server and later verified against the code_verifier.
     *
     * @param codeVerifier The code verifier to generate the challenge from
     * @return The Base64 URL-safe encoded SHA256 hash of the code verifier
     */
    fun generateCodeChallenge(codeVerifier: String): String {
        val bytes = codeVerifier.toByteArray(Charsets.US_ASCII)
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val digest = messageDigest.digest(bytes)
        return Base64.encodeToString(
            digest,
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )
    }
}
