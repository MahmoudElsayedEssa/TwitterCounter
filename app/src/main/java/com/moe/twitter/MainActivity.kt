package com.moe.twitter

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.moe.twitter.data.remote.api.TwitterOAuthService
import com.moe.twitter.data.remote.auth.TokenStorage
import com.moe.twitter.ui.theme.TwitterCounterTheme
import com.moe.twitter.presentation.twitter.TwitterScreen
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val tokenStorage: TokenStorage by inject()
    private val oauthService: TwitterOAuthService by inject()
    private var authCallbackHandled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle OAuth callback if present
        handleIntent(intent)

        setContent {
            TwitterCounterTheme {
                TwitterScreen()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val data = intent?.data

        // Check if this is an OAuth callback
        if (data?.scheme == "twittercounter" && data.host == "auth") {
            if (authCallbackHandled) {
                Log.d("MainActivity", "OAuth callback already handled, ignoring duplicate")
                return
            }
            val code = data.getQueryParameter("code")
            val error = data.getQueryParameter("error")
            val returnedState = data.getQueryParameter("state")
            val expectedState = tokenStorage.getState()

            when {
                code != null -> {
                    if (expectedState.isNullOrBlank() || expectedState != returnedState) {
                        Log.e("MainActivity", "OAuth state mismatch; ignoring callback.")
                        return
                    }
                    Log.d("MainActivity", "Received OAuth code: $code")
                    // Clear the intent data to prevent re-processing after recreate()
                    intent?.data = null
                    authCallbackHandled = true
                    exchangeCodeForToken(code)
                }
                error != null -> {
                    Log.e("MainActivity", "OAuth error: $error")
                }
            }
        }
    }

    private fun exchangeCodeForToken(code: String) {
        lifecycleScope.launch {
            try {
                val codeVerifier = tokenStorage.getCodeVerifier()
                if (codeVerifier == null) {
                    Log.e("MainActivity", "Code verifier not found")
                    return@launch
                }

                val response = oauthService.exchangeCodeForToken(
                    code = code,
                    clientId = BuildConfig.TWITTER_CLIENT_ID,
                    redirectUri = BuildConfig.TWITTER_REDIRECT_URI,
                    codeVerifier = codeVerifier
                )

                if (response.isSuccessful && response.body() != null) {
                    val tokenResponse = response.body()!!
                    tokenStorage.saveAccessToken(tokenResponse.accessToken)
                    tokenResponse.refreshToken?.let { tokenStorage.saveRefreshToken(it) }
                    tokenStorage.clearEphemeralAuth()

                    Log.d("MainActivity", "OAuth token exchange successful")

                    // Restart activity cleanly so ViewModel is recreated with new auth state
                    val restartIntent = Intent(this@MainActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(restartIntent)
                    finish()
                } else {
                    Log.e("MainActivity", "Token exchange failed: ${response.errorBody()?.string()}")
                    authCallbackHandled = false
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Token exchange error", e)
                // TODO: Show error to user
                authCallbackHandled = false
            }
        }
    }
}
