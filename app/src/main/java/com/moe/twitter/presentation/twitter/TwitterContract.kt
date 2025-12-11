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
sealed interface TwitterState {
    val text: String
    val maxChars: Int
    val metrics: TweetMetrics
    val errors: List<TextIssue>
    val isChecking: Boolean
    val postingState: PostingState
    val clearSignal: Int
    val isAuthenticated: Boolean

    @Immutable
    data class Content(
        override val text: String = "",
        override val maxChars: Int = 280,
        override val metrics: TweetMetrics = TweetMetrics(
            weightedLength = 0,
            remaining = 280,
            withinLimit = true
        ),
        override val errors: List<TextIssue> = emptyList(),
        override val isChecking: Boolean = false,
        override val postingState: PostingState = PostingState.Idle,
        override val clearSignal: Int = 0,
        override val isAuthenticated: Boolean = false
    ) : TwitterState
}

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


