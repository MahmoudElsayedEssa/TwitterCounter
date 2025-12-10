package com.moe.twitter.di

import com.moe.twitter.domain.usecase.CheckTextIssuesUseCase
import com.moe.twitter.domain.usecase.ComputeTweetMetricsUseCase
import com.moe.twitter.domain.usecase.PostTweetUseCase
import org.koin.dsl.module

private const val MAX_TWITTER_CHARACTERS = 280

val useCaseModule = module {
    factory { ComputeTweetMetricsUseCase(get(), maxCharacters = MAX_TWITTER_CHARACTERS) }
    factory { PostTweetUseCase(get()) }
    factory { CheckTextIssuesUseCase(get()) }
}


