package com.moe.twitter.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.nio.charset.Charset

/**
 * Captures non-2xx responses and logs their body content for easier debugging.
 * Leaves the chain flow untouched so Retrofit can still deserialize.
 */
class ErrorLoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (!response.isSuccessful) {
            val source = response.body?.source()
            source?.request(Long.MAX_VALUE)
            val buffer = source?.buffer
            val bodyString = buffer?.clone()?.readString(Charset.defaultCharset())
            // Use standard logging to avoid extra dependencies; OkHttp will tag it.
            if (!bodyString.isNullOrBlank()) {
                println("HTTP ${response.code} for ${request.method} ${request.url} -> $bodyString")
            }
        }

        return response
    }
}




