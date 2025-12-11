package com.moe.twitter.data.remote.api

import com.moe.twitter.data.remote.model.languagetool.LanguageToolResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface LanguageToolApi {

    @FormUrlEncoded
    @POST("check")
    suspend fun checkText(
        @Field("text") text: String,
        @Field("language") language: String = "en-US",
        @Field("disabledRules") disabledRules: String = "UPPERCASE_SENTENCE_START"
    ): LanguageToolResponse
}




