package com.moe.twitter.data.repository

import com.moe.twitter.data.remote.api.TwitterApiService
import com.moe.twitter.data.remote.model.PostTweetRequest
import com.moe.twitter.data.remote.model.PostTweetResponse
import com.moe.twitter.data.remote.model.TweetData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.net.UnknownHostException

@OptIn(ExperimentalCoroutinesApi::class)
class TweetRepositoryImplTest {

    private lateinit var repository: TweetRepositoryImpl
    private lateinit var twitterApiService: TwitterApiService
    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        twitterApiService = mockk()
        repository = TweetRepositoryImpl(twitterApiService, dispatcher)
    }

    @Test
    fun `calculateMetrics should use twitter text parser`() = runTest {
        val result = repository.calculateMetrics("Hello", 280)

        assertEquals(5, result.weightedLength)
        assertEquals(275, result.remaining)
        assertTrue(result.withinLimit)
    }

    @Test
    fun `publish should fail for blank text`() = runTest {
        val result = repository.publish("")

        assertTrue(result.isFailure)
        assertEquals(
            "Cannot publish an empty tweet.",
            result.exceptionOrNull()?.message
        )
        coVerify(exactly = 0) { twitterApiService.postTweet(any()) }
    }

    @Test
    fun `publish should return success when api returns data`() = runTest {
        val text = "Hello Twitter!"
        val request = PostTweetRequest(text)
        val responseBody = PostTweetResponse(data = TweetData(id = "123", text = text))

        coEvery { twitterApiService.postTweet(request) } returns Response.success(responseBody)

        val result = repository.publish(text)

        assertTrue(result.isSuccess)
        coVerify { twitterApiService.postTweet(request) }
    }

    @Test
    fun `publish should return failure when body is null`() = runTest {
        val text = "Hello Twitter!"
        coEvery { twitterApiService.postTweet(PostTweetRequest(text)) } returns Response.success(null)

        val result = repository.publish(text)

        assertTrue(result.isFailure)
        assertEquals("Empty response from server.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `publish should map unauthorized errors`() = runTest {
        val text = "Protected tweet"
        coEvery { twitterApiService.postTweet(PostTweetRequest(text)) } returns errorResponse(
            code = 401,
            body = """{"detail":"Unauthorized"}"""
        )

        val result = repository.publish(text)

        assertTrue(result.isFailure)
        assertEquals("Unauthorized. Please log in again.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `publish should map duplicate tweet errors`() = runTest {
        val text = "Duplicate tweet"
        coEvery { twitterApiService.postTweet(PostTweetRequest(text)) } returns errorResponse(
            code = 403,
            body = """{"detail":"duplicate content"}"""
        )

        val result = repository.publish(text)

        assertTrue(result.isFailure)
        assertEquals(
            "Duplicate tweet: change the text and try again.",
            result.exceptionOrNull()?.message
        )
    }

    @Test
    fun `publish should map exceptions via NetworkErrorMapper`() = runTest {
        val text = "Any tweet"
        coEvery { twitterApiService.postTweet(PostTweetRequest(text)) } throws UnknownHostException("no connection")

        val result = repository.publish(text)

        assertTrue(result.isFailure)
        assertEquals("No client available", result.exceptionOrNull()?.message)
    }

    private fun errorResponse(code: Int, body: String?): Response<PostTweetResponse> {
        val mediaType = "application/json".toMediaType()
        val responseBody = (body ?: "").toResponseBody(mediaType)
        return Response.error(code, responseBody)
    }
}
