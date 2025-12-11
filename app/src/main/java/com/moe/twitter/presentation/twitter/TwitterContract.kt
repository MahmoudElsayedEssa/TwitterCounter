package com.moe.twitter.presentation.twitter

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextLayoutResult
import com.moe.twitter.domain.model.TextIssue
import com.moe.twitter.domain.model.TweetMetrics

/**
 * Represents the posting state using a sealed class for type safety and exhaustive handling.
 */
sealed interface PostingState {
    data object Idle : PostingState
    data object Posting : PostingState
    data object Success : PostingState
    data class Error(val message: String) : PostingState
}

@Immutable
data class TwitterState(
    val text: String = "",
    val maxChars: Int = 280,
    val metrics: TweetMetrics = TweetMetrics(
        weightedLength = 0,
        remaining = 280,
        withinLimit = true
    ),
    val errors: List<TextIssue> = emptyList(),
    val isChecking: Boolean = false,
    val postingState: PostingState = PostingState.Idle,
    val clearSignal: Int = 0,
    val isAuthenticated: Boolean = false
)

interface TwitterAction {
    data class OnTextChange(val value: String) : TwitterAction
    data object OnClear : TwitterAction
    data object OnCopy : TwitterAction
    data object OnPost : TwitterAction
    data class OnTextLayout(val layout: TextLayoutResult) : TwitterAction
    data object OnLogin : TwitterAction
    data object OnLogout : TwitterAction
}

@Immutable
data class GhostSeed(
    val id: Long,
    val char: Char,
    val baseX: Float,
    val baseY: Float,
    val order: Int = 0
)

sealed interface GhostEvent {
    data class Backspace(val seed: GhostSeed, val delayMs: Long) : GhostEvent
    data class Clear(val seeds: List<GhostSeed>, val smartDelayMs: Long) : GhostEvent
}

sealed interface TwitterEffect {
    data class ShowToast(val message: String) : TwitterEffect
    data class CopyToClipboard(val text: String) : TwitterEffect
}

/**
 * Ghost info the UI needs to render the dissolving characters.
 */
@Immutable
data class GhostCharUi(
    val id: Long,
    val char: Char,
    val baseX: Float,
    val baseY: Float,
    val offsetX: Float,
    val offsetY: Float,
    val alpha: Float,
    val scale: Float,
    val rotation: Float
)


