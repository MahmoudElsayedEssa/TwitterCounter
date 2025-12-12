package com.moe.twitter.data.remote.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Secure token storage implementation using EncryptedSharedPreferences.
 *
 * This implementation encrypts tokens at rest for enhanced security.
 */
class SharedPreferencesTokenStorage(context: Context) : TokenStorage {

    private val sharedPreferences: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override fun saveAccessToken(token: String) {
        sharedPreferences.edit {
            putString(KEY_ACCESS_TOKEN, token)
        }
    }

    override fun getAccessToken(): String? {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    }

    override fun saveRefreshToken(token: String) {
        sharedPreferences.edit {
            putString(KEY_REFRESH_TOKEN, token)
        }
    }

    override fun getRefreshToken(): String? {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    }

    override fun saveCodeVerifier(codeVerifier: String) {
        sharedPreferences.edit {
            putString(KEY_CODE_VERIFIER, codeVerifier)
        }
    }

    override fun getCodeVerifier(): String? {
        return sharedPreferences.getString(KEY_CODE_VERIFIER, null)
    }

    override fun saveState(state: String) {
        sharedPreferences.edit {
            putString(KEY_STATE, state)
        }
    }

    override fun getState(): String? {
        return sharedPreferences.getString(KEY_STATE, null)
    }

    override fun clearEphemeralAuth() {
        sharedPreferences.edit {
            remove(KEY_CODE_VERIFIER)
                .remove(KEY_STATE)
        }
    }

    override fun clearTokens() {
        sharedPreferences.edit {
            remove(KEY_ACCESS_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .remove(KEY_CODE_VERIFIER)
                .remove(KEY_STATE)
        }
    }

    companion object {
        private const val PREFS_NAME = "twitter_oauth_tokens"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_CODE_VERIFIER = "code_verifier"
        private const val KEY_STATE = "oauth_state"
    }
}
