package com.moe.twitter.domain.usecase

import com.moe.twitter.domain.TwitterConstants
import com.moe.twitter.domain.model.TweetMetrics
import com.moe.twitter.domain.repository.TweetRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ComputeTweetMetricsUseCaseTest {

    private lateinit var tweetRepository: TweetRepository
    private lateinit var useCase: ComputeTweetMetricsUseCase

    @Before
    fun setup() {
        tweetRepository = mockk()
        useCase = ComputeTweetMetricsUseCase(
            calculator = tweetRepository,
            maxCharacters = TwitterConstants.MAX_TWEET_CHARS
        )
    }

    @Test
    fun `invoke should delegate to repository with correct parameters`() = runTest {
        val testText = "Hello Twitter!"
        val expectedMetrics = TweetMetrics(
            weightedLength = 14,
            remaining = 266,
            withinLimit = true
        )

        coEvery {
            tweetRepository.calculateMetrics(testText, TwitterConstants.MAX_TWEET_CHARS)
        } returns expectedMetrics

        val result = useCase(testText)

        assertEquals(expectedMetrics, result)
        coVerify {
            tweetRepository.calculateMetrics(testText, TwitterConstants.MAX_TWEET_CHARS)
        }
    }

    @Test
    fun `invoke with empty text should return zero metrics`() = runTest {
        val emptyText = ""
        val expectedMetrics = TweetMetrics(
            weightedLength = 0,
            remaining = TwitterConstants.MAX_TWEET_CHARS,
            withinLimit = true
        )

        coEvery {
            tweetRepository.calculateMetrics(emptyText, TwitterConstants.MAX_TWEET_CHARS)
        } returns expectedMetrics

        val result = useCase(emptyText)

        assertEquals(expectedMetrics, result)
        coVerify {
            tweetRepository.calculateMetrics(emptyText, TwitterConstants.MAX_TWEET_CHARS)
        }
    }

    @Test
    fun `invoke with text exceeding limit should return withinLimit false`() = runTest {
        val longText = "a".repeat(300)
        val expectedMetrics = TweetMetrics(
            weightedLength = 300,
            remaining = -20,
            withinLimit = false
        )

        coEvery {
            tweetRepository.calculateMetrics(longText, TwitterConstants.MAX_TWEET_CHARS)
        } returns expectedMetrics

        val result = useCase(longText)

        assertEquals(expectedMetrics, result)
        assertEquals(false, result.withinLimit)
        coVerify {
            tweetRepository.calculateMetrics(longText, TwitterConstants.MAX_TWEET_CHARS)
        }
    }

    @Test
    fun `invoke with exact limit text should return zero remaining`() = runTest {
        val limitText = "a".repeat(TwitterConstants.MAX_TWEET_CHARS)
        val expectedMetrics = TweetMetrics(
            weightedLength = TwitterConstants.MAX_TWEET_CHARS,
            remaining = 0,
            withinLimit = true
        )

        coEvery {
            tweetRepository.calculateMetrics(limitText, TwitterConstants.MAX_TWEET_CHARS)
        } returns expectedMetrics

        val result = useCase(limitText)

        assertEquals(expectedMetrics, result)
        assertEquals(0, result.remaining)
        assertEquals(true, result.withinLimit)
    }
}
