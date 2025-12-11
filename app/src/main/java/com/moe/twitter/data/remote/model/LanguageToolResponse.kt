package com.moe.twitter.data.remote.model

import com.google.gson.annotations.SerializedName

data class LanguageToolResponse(
    @SerializedName("matches")
    val matches: List<LanguageToolMatch> = emptyList()
)

data class LanguageToolMatch(
    @SerializedName("offset")
    val offset: Int?,
    @SerializedName("length")
    val length: Int?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("rule")
    val rule: LanguageToolRule?
)

data class LanguageToolRule(
    @SerializedName("id")
    val id: String?,
    @SerializedName("issueType")
    val issueType: String?
)




