package com.moe.twitter.data.repository

import com.moe.twitter.data.remote.NetworkErrorMapper
import com.moe.twitter.data.remote.api.TwitterApiService
import com.moe.twitter.data.remote.model.PostTweetRequest
import com.moe.twitter.domain.model.TweetPublishException
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
        return TweetMetrics(
            weightedLength = weighted,
            remaining = maxCharacters - weighted,
            withinLimit = weighted <= maxCharacters
        )

    }

    override suspend fun publish(text: String): Result<Unit> = withContext(ioDispatcher) {
        if (text.isBlank()) {
            return@withContext Result.failure(TweetPublishException("Cannot publish an empty tweet."))
        }

        try {
            val response = twitterApiService.postTweet(PostTweetRequest(text = text))
            if (!response.isSuccessful) {
                return@withContext NetworkErrorMapper.fromResponse(response)
            }

            val body = response.body()
            when {
                body?.data != null -> Result.success(Unit)
                body == null -> Result.failure(TweetPublishException("Empty response from server."))
                else -> Result.failure(TweetPublishException("Unexpected response from server."))
            }
        } catch (e: Exception) {
            return@withContext NetworkErrorMapper.fromException(e)
        }
    }

}