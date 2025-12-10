package com.moe.twitter

import android.app.Application
import com.moe.twitter.di.networkModule
import com.moe.twitter.di.repositoryModule
import com.moe.twitter.di.useCaseModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class TwitterCounterApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TwitterCounterApp)
            modules(
                listOf(
                    networkModule,
                    repositoryModule,
                    useCaseModule
                )
            )
        }
    }
}


