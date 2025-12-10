package com.moe.twitter.presentation.twitter

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextLayoutResult
import com.moe.twitter.domain.model.TextIssue
import com.moe.twitter.domain.model.TweetMetrics

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
    val isPosting: Boolean = false,
    val clearSignal: Int = 0
)

interface TwitterAction {
    data class OnTextChange(val value: String) : TwitterAction
    data object OnClear : TwitterAction
    data object OnCopy : TwitterAction
    data object OnPost : TwitterAction
    data class OnTextLayout(val layout: TextLayoutResult) : TwitterAction
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


