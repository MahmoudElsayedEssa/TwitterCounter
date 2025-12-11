package com.moe.twitter.domain.repository

import com.moe.twitter.domain.model.TweetMetrics

interface TweetRepository {
    suspend fun calculateMetrics(text: String, maxCharacters: Int): TweetMetrics
    suspend fun publish(text: String): Result<Unit>
}