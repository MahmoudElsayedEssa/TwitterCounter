package com.moe.twitter.presentation.twitter.components

import androidx.compose.runtime.Immutable

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
