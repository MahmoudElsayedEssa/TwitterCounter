package com.moe.twitter.domain

/**
 * Twitter-specific constants used across the presentation layer.
 */
object TwitterConstants {
    /**
     * Maximum characters allowed in a tweet.
     * Standard Twitter limit is 280 characters (was 140 before 2017).
     * Premium accounts may have higher limits.
     */
    const val MAX_TWEET_CHARS = 280
}