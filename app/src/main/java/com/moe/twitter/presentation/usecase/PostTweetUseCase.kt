package com.moe.twitter.presentation.usecase

import com.moe.twitter.presentation.model.PostTweetResult
import com.moe.twitter.presentation.repository.TweetRepository

class PostTweetUseCase(
    private val publisher: TweetRepository
) {
    suspend operator fun invoke(text: String): PostTweetResult = publisher.publish(text)
}

