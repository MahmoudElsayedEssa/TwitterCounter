package com.moe.twitter.presentation.model

data class TweetMetrics(
    val weightedLength: Int,
    val remaining: Int,
    val withinLimit: Boolean
)

