package com.moe.twitter.presentation.usecase

import com.moe.twitter.presentation.model.TweetMetrics
import com.moe.twitter.presentation.repository.TweetRepository

class ComputeTweetMetricsUseCase(
    private val calculator: TweetRepository,
    private val maxCharacters: Int
) {
    suspend operator fun invoke(text: String): TweetMetrics =
        calculator.calculateMetrics(text = text, maxCharacters = maxCharacters)

}

