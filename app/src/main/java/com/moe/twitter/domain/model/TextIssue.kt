package com.moe.twitter.domain.model

/**
 * Represents a grammar/spelling/style issue returned from LanguageTool.
 */
data class TextIssue(
    val start: Int,
    val end: Int,
    val message: String? = null,
    val issueType: String? = null,
    val ruleId: String? = null
)


