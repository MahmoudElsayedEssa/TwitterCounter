package com.moe.twitter.data.repository

import com.moe.twitter.data.remote.NetworkErrorMapper
import com.moe.twitter.data.remote.api.TwitterApiService
import com.moe.twitter.data.remote.model.PostTweetRequest
import com.moe.twitter.domain.model.PostTweetResult
import com.moe.twitter.domain.model.TweetMetrics
import com.moe.twitter.domain.repository.TweetRepository
import com.twitter.twittertext.TwitterTextParser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TweetRepositoryImpl(
    private val twitterApiService: TwitterApiService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : TweetRepository {
    override suspend fun calculateMetrics(text: String, maxCharacters: Int): TweetMetrics {
        val parsed = TwitterTextParser.parseTweet(text)
        val weighted = parsed.weightedLength
        val remaining = (maxCharacters - weighted).coerceAtLeast(0)
        val withinLimit = weighted <= maxCharacters
        return TweetMetrics(
            weightedLength = weighted,
            remaining = remaining,
            withinLimit = withinLimit
        )

    }

    override suspend fun publish(text: String): PostTweetResult = withContext(ioDispatcher) {
        if (text.isBlank()) {
            return@withContext PostTweetResult.Failure("Cannot publish an empty tweet.")
        }

        try {
            val response = twitterApiService.postTweet(PostTweetRequest(text = text))
            if (!response.isSuccessful) {
                return@withContext NetworkErrorMapper.fromResponse(response)
            }

            val body = response.body()
            when {
                body?.success == true -> PostTweetResult.Success
                body == null -> PostTweetResult.Failure("Empty response from server.")
                else -> PostTweetResult.Failure(body.message)
            }
        } catch (e: Exception) {
            NetworkErrorMapper.fromException(e)
        }
    }

}