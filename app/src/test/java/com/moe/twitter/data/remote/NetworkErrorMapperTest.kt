package com.moe.twitter.data.remote

import com.moe.twitter.data.remote.model.PostTweetResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class NetworkErrorMapperTest {

    @Test
    fun `fromException should map common network throwables`() {
        val noHost = NetworkErrorMapper.fromException(UnknownHostException("dns"))
        val timeout = NetworkErrorMapper.fromException(SocketTimeoutException("slow"))
        val io = NetworkErrorMapper.fromException(IOException("broken pipe"))
        val unknown = NetworkErrorMapper.fromException(IllegalStateException("weird"))

        assertEquals("No client available", noHost.exceptionOrNull()?.message)
        assertEquals(
            "Request timed out. Please check your connection.",
            timeout.exceptionOrNull()?.message
        )
        assertEquals(
            "Network error: broken pipe",
            io.exceptionOrNull()?.message
        )
        assertEquals("weird", unknown.exceptionOrNull()?.message)
    }

    @Test
    fun `fromResponse should parse detail field for 400 errors`() {
        val response = errorResponse(
            code = 400,
            body = """{"detail":"Specific validation failure"}"""
        )

        val result = NetworkErrorMapper.fromResponse(response)
        assertTrue(result.isFailure)
        assertEquals("Specific validation failure", result.exceptionOrNull()?.message)
    }

    @Test
    fun `fromResponse should return duplicate tweet message for 403 duplicate`() {
        val response = errorResponse(
            code = 403,
            body = """{"detail":"Duplicate content detected"}"""
        )

        val result = NetworkErrorMapper.fromResponse(response)
        assertTrue(result.isFailure)
        assertEquals(
            "Duplicate tweet: change the text and try again.",
            result.exceptionOrNull()?.message
        )
    }

    @Test
    fun `fromResponse should handle common status codes`() {
        val rateLimited = NetworkErrorMapper.fromResponse(errorResponse(429, null))
        val serverError = NetworkErrorMapper.fromResponse(errorResponse(500, null))
        val notFound = NetworkErrorMapper.fromResponse(errorResponse(404, ""))

        assertEquals("Rate limited. Try again shortly.", rateLimited.exceptionOrNull()?.message)
        assertEquals(
            "Server error (500). Please retry.",
            serverError.exceptionOrNull()?.message
        )
        assertEquals("Endpoint not found.", notFound.exceptionOrNull()?.message)
    }

    private fun errorResponse(code: Int, body: String?): Response<PostTweetResponse> {
        val mediaType = "application/json".toMediaType()
        val responseBody = (body ?: "").toResponseBody(mediaType)
        return Response.error(code, responseBody)
    }
}
