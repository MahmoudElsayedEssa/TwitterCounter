package com.moe.twitter.domain.model

/**
 * Represents a user-facing failure when publishing a tweet.
 */
class TweetPublishException(message: String) : Exception(message)
