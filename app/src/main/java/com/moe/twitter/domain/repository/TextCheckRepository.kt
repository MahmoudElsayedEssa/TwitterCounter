package com.moe.twitter.domain.repository

import com.moe.twitter.domain.model.TextIssue

interface TextCheckRepository {
    suspend fun checkTextIssues(text: String, language: String = "en-US"): Result<List<TextIssue>>
}




