package com.moe.twitter.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Adds basic headers to every request to keep APIs happy and traceable.
 */
class DefaultHeadersInterceptor(
    private val userAgent: String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()
            .header("Accept", "application/json")
            .header("User-Agent", userAgent)

        // Respect content-type already set by Retrofit (e.g., form-url-encoded).
        if (original.header("Content-Type") == null) {
            builder.header("Content-Type", "application/json")
        }

        val newRequest = builder.build()

        return chain.proceed(newRequest)
    }
}


