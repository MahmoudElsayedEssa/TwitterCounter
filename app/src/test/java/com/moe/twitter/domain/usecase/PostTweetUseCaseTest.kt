package com.moe.twitter.domain.usecase

import com.moe.twitter.domain.model.TweetPublishException
import com.moe.twitter.domain.repository.TweetRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PostTweetUseCaseTest {

    private lateinit var tweetRepository: TweetRepository
    private lateinit var useCase: PostTweetUseCase

    @Before
    fun setup() {
        tweetRepository = mockk()
        useCase = PostTweetUseCase(publisher = tweetRepository)
    }

    @Test
    fun `invoke should successfully publish tweet`() = runTest {
        val testText = "Hello Twitter!"

        coEvery { tweetRepository.publish(testText) } returns Result.success(Unit)

        val result = useCase(testText)

        assertTrue(result.isSuccess)
        coVerify { tweetRepository.publish(testText) }
    }

    @Test
    fun `invoke should return failure when repository fails`() = runTest {
        val testText = "Hello Twitter!"
        val errorMessage = "Network error"

        coEvery {
            tweetRepository.publish(testText)
        } returns Result.failure(Exception(errorMessage))

        val result = useCase(testText)

        assertTrue(result.isFailure)
        assertEquals(errorMessage, result.exceptionOrNull()?.message)
        coVerify { tweetRepository.publish(testText) }
    }

    @Test
    fun `invoke should return TweetPublishException when no client available`() = runTest {
        val testText = "Hello Twitter!"

        coEvery {
            tweetRepository.publish(testText)
        } returns Result.failure(TweetPublishException("No client available"))

        val result = useCase(testText)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is TweetPublishException)
        assertEquals("No client available", result.exceptionOrNull()?.message)
        coVerify { tweetRepository.publish(testText) }
    }

    @Test
    fun `invoke with empty text should still call repository`() = runTest {
        val emptyText = ""

        coEvery { tweetRepository.publish(emptyText) } returns Result.success(Unit)

        val result = useCase(emptyText)

        assertTrue(result.isSuccess)
        coVerify { tweetRepository.publish(emptyText) }
    }

    @Test
    fun `invoke with long text should delegate to repository`() = runTest {
        val longText = "a".repeat(500)

        coEvery { tweetRepository.publish(longText) } returns Result.success(Unit)

        val result = useCase(longText)

        assertTrue(result.isSuccess)
        coVerify { tweetRepository.publish(longText) }
    }
}
