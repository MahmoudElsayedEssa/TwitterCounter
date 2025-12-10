package com.moe.twitter.domain.usecase

import com.moe.twitter.domain.model.TweetMetrics
import com.moe.twitter.domain.repository.TweetRepository

class ComputeTweetMetricsUseCase(
    private val calculator: TweetRepository,
    private val maxCharacters: Int
) {
    suspend operator fun invoke(text: String): TweetMetrics =
        calculator.calculateMetrics(text = text, maxCharacters = maxCharacters)

}

