package com.moe.twitter.di

import com.moe.twitter.BuildConfig
import com.moe.twitter.data.remote.api.LanguageToolApi
import com.moe.twitter.data.remote.api.TwitterApiService
import com.moe.twitter.data.remote.interceptor.DefaultHeadersInterceptor
import com.moe.twitter.data.remote.interceptor.ErrorLoggingInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private const val QUALIFIER_TWITTER = "twitterRetrofit"
private const val QUALIFIER_LANGUAGE_TOOL = "languageToolRetrofit"

val networkModule = module {

    single {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    single { DefaultHeadersInterceptor(userAgent = "TwitterCounter/${BuildConfig.VERSION_NAME}") }
    single { ErrorLoggingInterceptor() }

    single {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(get<DefaultHeadersInterceptor>())
            .addInterceptor(get<ErrorLoggingInterceptor>())
            .addInterceptor(get<HttpLoggingInterceptor>())
            .build()
    }

    single(named(QUALIFIER_TWITTER)) {
        Retrofit.Builder()
            .baseUrl(BuildConfig.TWITTER_BASE_URL)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single(named(QUALIFIER_LANGUAGE_TOOL)) {
        Retrofit.Builder()
            .baseUrl(BuildConfig.LANGUAGE_TOOL_BASE_URL)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single {
        get<Retrofit>(named(QUALIFIER_TWITTER)).create(TwitterApiService::class.java)
    }

    single {
        get<Retrofit>(named(QUALIFIER_LANGUAGE_TOOL)).create(LanguageToolApi::class.java)
    }
}


