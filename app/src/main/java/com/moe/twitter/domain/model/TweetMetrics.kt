package com.moe.twitter.domain.model

data class TweetMetrics(
    val weightedLength: Int,
    val remaining: Int,
    val withinLimit: Boolean
)

