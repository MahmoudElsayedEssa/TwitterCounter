package com.moe.twitter.di

import com.moe.twitter.data.repository.TextCheckRepositoryImpl
import com.moe.twitter.data.repository.TweetRepositoryImpl
import com.moe.twitter.domain.repository.TextCheckRepository
import com.moe.twitter.domain.repository.TweetRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<TweetRepository> { TweetRepositoryImpl(get()) }
    single<TextCheckRepository> { TextCheckRepositoryImpl(get()) }
}


