package com.moe.twitter.data.remote.interceptor

import com.moe.twitter.data.remote.auth.TwitterTokenProvider
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Intercepts outgoing requests and adds OAuth Authorization header.
 *
 * Adds "Authorization: Bearer {token}" header to all requests
 * if a token is available from the token provider.
 */
class TwitterAuthInterceptor(
    private val tokenProvider: TwitterTokenProvider
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val token = tokenProvider.getToken()
        if (token.isNullOrBlank()) {
            return chain.proceed(original)
        }

        val authenticated = original.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(authenticated)
    }
}
