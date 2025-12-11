package com.moe.twitter.domain.usecase

import com.moe.twitter.domain.model.AuthTokens
import com.moe.twitter.domain.repository.AuthRepository

class ExchangeTwitterCodeUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        code: String,
        redirectUri: String,
        codeVerifier: String,
        clientId: String
    ): Result<AuthTokens> = repository.exchangeCode(
        code = code,
        redirectUri = redirectUri,
        codeVerifier = codeVerifier,
        clientId = clientId
    )
}


