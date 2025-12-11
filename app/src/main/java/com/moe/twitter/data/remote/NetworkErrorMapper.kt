package com.moe.twitter.data.remote

import com.moe.twitter.data.remote.model.PostTweetResponse
import com.moe.twitter.domain.model.TweetPublishException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object NetworkErrorMapper {

    fun fromException(throwable: Throwable): Result<Unit> =
        when (throwable) {
            is UnknownHostException -> Result.failure(TweetPublishException("No client available"))
            is SocketTimeoutException -> Result.failure(TweetPublishException("Request timed out. Please check your connection."))
            is IOException -> Result.failure(TweetPublishException("Network error: ${throwable.message}"))
            else -> Result.failure(TweetPublishException(throwable.message ?: "Unexpected error"))
        }

    fun fromResponse(response: Response<PostTweetResponse>): Result<Unit> {
        val errorMessage = response.errorBody()?.string()
        val fallback = parseDetail(errorMessage) ?: errorMessage?.takeIf { it.isNotBlank() }
        // Twitter duplicate protection returns 403 with a detail message
        if (response.code() == 403 && errorMessage?.contains("duplicate content", ignoreCase = true) == true) {
            return Result.failure(TweetPublishException("Duplicate tweet: change the text and try again."))
        }

        val message = when (response.code()) {
            400 -> fallback ?: "Bad request."
            401 -> "Unauthorized. Please log in again."
            403 -> fallback ?: "Forbidden request."
            404 -> "Endpoint not found."
            429 -> "Rate limited. Try again shortly."
            in 500..599 -> "Server error (${response.code()}). Please retry."
            else -> fallback ?: "Unexpected error (${response.code()})."
        }
        return Result.failure(TweetPublishException(message))
    }

    private fun parseDetail(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val regex = """"detail"\s*:\s*"([^"]+)"""".toRegex()
        return regex.find(raw)?.groupValues?.getOrNull(1)
    }
}




