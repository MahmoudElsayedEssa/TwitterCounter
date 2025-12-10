package com.moe.twitter.presentation.repository

import com.moe.twitter.presentation.model.PostTweetResult
import com.moe.twitter.presentation.model.TweetMetrics

interface TweetRepository {
    suspend fun calculateMetrics(text: String, maxCharacters: Int): TweetMetrics
    suspend fun publish(text: String): PostTweetResult
}