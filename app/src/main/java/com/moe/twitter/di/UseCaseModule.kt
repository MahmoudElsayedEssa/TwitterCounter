package com.moe.twitter.di

import com.moe.twitter.domain.usecase.CheckTextIssuesUseCase
import com.moe.twitter.domain.usecase.ComputeTweetMetricsUseCase
import com.moe.twitter.domain.usecase.PostTweetUseCase
import com.moe.twitter.domain.TwitterConstants
import org.koin.dsl.module

val useCaseModule = module {
    factory { ComputeTweetMetricsUseCase(get(), maxCharacters = TwitterConstants.MAX_TWEET_CHARS) }
    factory { PostTweetUseCase(get()) }
    factory { CheckTextIssuesUseCase(get()) }
}


