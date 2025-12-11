package com.moe.twitter.domain.usecase

import com.moe.twitter.domain.repository.TweetRepository

class PostTweetUseCase(
    private val publisher: TweetRepository
) {
    suspend operator fun invoke(text: String): Result<Unit> = publisher.publish(text)
}

