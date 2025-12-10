package com.moe.twitter.presentation.model

sealed class PostTweetResult {
    data object Success : PostTweetResult()
    data object NoClientAvailable : PostTweetResult()
    data class Failure(val message: String? = null) : PostTweetResult()
}

