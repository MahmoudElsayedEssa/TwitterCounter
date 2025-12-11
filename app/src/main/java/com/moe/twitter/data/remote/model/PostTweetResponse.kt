package com.moe.twitter.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Matches the actual Twitter v2 POST /2/tweets response:
 * {
 *   "data": { "id": "...", "text": "..." }
 * }
 */
data class PostTweetResponse(
    val data: TweetData?
)

data class TweetData(
    val id: String?,
    @SerializedName("edit_history_tweet_ids") val editHistoryTweetIds: List<String>? = null,
    val text: String? = null
)
