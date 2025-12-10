package com.moe.twitter.data.repository

import com.moe.twitter.data.remote.api.LanguageToolApi
import com.moe.twitter.domain.model.TextIssue
import com.moe.twitter.domain.repository.TextCheckRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TextCheckRepositoryImpl(
    private val languageToolApi: LanguageToolApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : TextCheckRepository {

    override suspend fun checkTextIssues(
        text: String,
        language: String
    ): List<TextIssue> = withContext(ioDispatcher) {
        if (text.isBlank()) return@withContext emptyList()

        try {
            val response = languageToolApi.checkText(
                text = text,
                language = language
            )

            response.matches.mapNotNull { match ->
                val offset = match.offset ?: return@mapNotNull null
                val length = match.length ?: return@mapNotNull null

                val start = offset.coerceIn(0, text.length)
                val end = (offset + length).coerceIn(start, text.length)
                if (start >= end) return@mapNotNull null

                TextIssue(
                    start = start,
                    end = end,
                    message = match.message,
                    issueType = match.rule?.issueType,
                    ruleId = match.rule?.id
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}


