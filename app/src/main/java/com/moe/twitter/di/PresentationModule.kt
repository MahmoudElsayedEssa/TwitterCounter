package com.moe.twitter.di

import com.moe.twitter.presentation.twitter.TwitterViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    viewModel {
        TwitterViewModel(
            postTweetUseCase = get(),
            checkTextIssuesUseCase = get(),
            computeTweetMetricsUseCase = get(),
            oauthManager = get()
        )
    }
}


