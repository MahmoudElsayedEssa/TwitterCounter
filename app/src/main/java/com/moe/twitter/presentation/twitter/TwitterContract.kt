package com.moe.twitter.presentation.twitter

import androidx.compose.runtime.Immutable
import com.moe.twitter.domain.model.TextIssue
import com.moe.twitter.domain.model.TweetMetrics

sealed interface PostingState {
    data object Idle : PostingState
    data object Posting : PostingState
    data object Success : PostingState
    data class Error(val message: String) : PostingState
}

@Immutable
data class TwitterState(
    val text: String = "",
    val metrics: TweetMetrics = TweetMetrics(
        weightedLength = 0,
        remaining = 280,
        withinLimit = true
    ),
    val errors: List<TextIssue> = emptyList(),
    val isChecking: Boolean = false,
    val postingState: PostingState = PostingState.Idle,
    val isAuthenticated: Boolean = false
)

sealed interface TwitterAction {
    data class OnTextChange(val value: String) : TwitterAction
    data object OnClear : TwitterAction
    data object OnCopy : TwitterAction
    data object OnPost : TwitterAction
    data object OnLogout : TwitterAction
}

sealed interface TwitterEffect {
    data class ShowToast(val message: String) : TwitterEffect
    data class CopyToClipboard(val text: String) : TwitterEffect
}


