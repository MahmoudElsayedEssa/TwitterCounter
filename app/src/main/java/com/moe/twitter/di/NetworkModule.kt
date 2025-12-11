package com.moe.twitter.di

import android.content.Context
import com.moe.twitter.BuildConfig
import com.moe.twitter.data.remote.api.LanguageToolApi
import com.moe.twitter.data.remote.api.TwitterApiService
import com.moe.twitter.data.remote.api.TwitterOAuthService
import com.moe.twitter.data.remote.auth.OAuthManager
import com.moe.twitter.data.remote.auth.SharedPreferencesTokenStorage
import com.moe.twitter.data.remote.auth.StorageTwitterTokenProvider
import com.moe.twitter.data.remote.auth.TokenStorage
import com.moe.twitter.data.remote.auth.TwitterTokenProvider
import com.moe.twitter.data.remote.interceptor.DefaultHeadersInterceptor
import com.moe.twitter.data.remote.interceptor.ErrorLoggingInterceptor
import com.moe.twitter.data.remote.interceptor.TwitterAuthInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private const val QUALIFIER_TWITTER = "twitterRetrofit"
private const val QUALIFIER_TWITTER_OAUTH = "twitterOAuthRetrofit"
private const val QUALIFIER_LANGUAGE_TOOL = "languageToolRetrofit"

val networkModule = module {

    // OAuth Token Storage
    single<TokenStorage> { SharedPreferencesTokenStorage(context = androidContext()) }

    // OAuth Token Provider - switch based on auth state
    single<TwitterTokenProvider> {
        // Always read latest from storage; avoids sticking with app-only token
        StorageTwitterTokenProvider(tokenStorage = get())
    }

    // OAuth Manager
    single { OAuthManager(context = androidContext(), tokenStorage = get()) }

    single {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    single { DefaultHeadersInterceptor(userAgent = "TwitterCounter/${BuildConfig.VERSION_NAME}") }
    single { ErrorLoggingInterceptor() }
    single { TwitterAuthInterceptor(tokenProvider = get()) }

    single {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(get<TwitterAuthInterceptor>())
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

    single(named(QUALIFIER_TWITTER_OAUTH)) {
        // OAuth Retrofit without auth interceptor (for token exchange)
        val oauthClient = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(get<DefaultHeadersInterceptor>())
            .addInterceptor(get<HttpLoggingInterceptor>())
            .build()

        Retrofit.Builder()
            .baseUrl("https://api.twitter.com/2/")
            .client(oauthClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single {
        get<Retrofit>(named(QUALIFIER_TWITTER)).create(TwitterApiService::class.java)
    }

    single {
        get<Retrofit>(named(QUALIFIER_LANGUAGE_TOOL)).create(LanguageToolApi::class.java)
    }

    single {
        get<Retrofit>(named(QUALIFIER_TWITTER_OAUTH)).create(TwitterOAuthService::class.java)
    }
}


