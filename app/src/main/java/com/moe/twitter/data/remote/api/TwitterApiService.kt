package com.moe.twitter.data.remote.api

import com.moe.twitter.data.remote.model.PostTweetRequest
import com.moe.twitter.data.remote.model.PostTweetResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface TwitterApiService {

    @POST("tweets")
    suspend fun postTweet(
        @Body request: PostTweetRequest
    ): Response<PostTweetResponse>
}




