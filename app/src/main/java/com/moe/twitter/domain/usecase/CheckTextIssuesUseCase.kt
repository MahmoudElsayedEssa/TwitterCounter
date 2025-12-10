package com.moe.twitter.domain.usecase

import com.moe.twitter.domain.model.TextIssue
import com.moe.twitter.domain.repository.TweetRepository

class CheckTextIssuesUseCase(
    private val repository: TweetRepository
) {
    suspend operator fun invoke(text: String, language: String = "en-US"): List<TextIssue> =
        repository.checkTextIssues(text = text, language = language)
}


