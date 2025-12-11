package com.moe.twitter.data.remote

import com.moe.twitter.data.remote.model.PostTweetResponse
import com.moe.twitter.domain.model.PostTweetResult
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object NetworkErrorMapper {

    fun fromException(throwable: Throwable): PostTweetResult =
        when (throwable) {
            is UnknownHostException -> PostTweetResult.NoClientAvailable
            is SocketTimeoutException -> PostTweetResult.Failure("Request timed out. Please check your connection.")
            is IOException -> PostTweetResult.Failure("Network error: ${throwable.message}")
            else -> PostTweetResult.Failure(throwable.message)
        }

    fun fromResponse(response: Response<PostTweetResponse>): PostTweetResult {
        val errorMessage = response.errorBody()?.string()
        val fallback = errorMessage?.takeIf { it.isNotBlank() }

        return when (response.code()) {
            400 -> PostTweetResult.Failure(fallback ?: "Bad request.")
            401 -> PostTweetResult.Failure("Unauthorized. Please log in again.")
            403 -> PostTweetResult.Failure("Forbidden request.")
            404 -> PostTweetResult.Failure("Endpoint not found.")
            429 -> PostTweetResult.Failure("Rate limited. Try again shortly.")
            in 500..599 -> PostTweetResult.Failure("Server error (${response.code()}). Please retry.")
            else -> PostTweetResult.Failure(fallback ?: "Unexpected error (${response.code()}).")
        }
    }
}




