package com.moe.twitter.domain.repository

import com.moe.twitter.domain.model.PostTweetResult
import com.moe.twitter.domain.model.TextIssue
import com.moe.twitter.domain.model.TweetMetrics

interface TweetRepository {
    suspend fun calculateMetrics(text: String, maxCharacters: Int): TweetMetrics
    suspend fun publish(text: String): PostTweetResult
    suspend fun checkTextIssues(text: String, language: String = "en-US"): List<TextIssue>
}