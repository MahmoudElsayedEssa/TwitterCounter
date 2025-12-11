package com.moe.twitter.data.remote.api

import com.moe.twitter.data.remote.model.TokenResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Retrofit service for Twitter OAuth 2.0 token exchange.
 */
interface TwitterOAuthService {

    @FormUrlEncoded
    @POST("oauth2/token")
    suspend fun exchangeCodeForToken(
        @Field("code") code: String,
        @Field("grant_type") grantType: String = "authorization_code",
        @Field("client_id") clientId: String,
        @Field("redirect_uri") redirectUri: String,
        @Field("code_verifier") codeVerifier: String
    ): Response<TokenResponse>
}
