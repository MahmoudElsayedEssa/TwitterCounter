package com.moe.twitter.presentation.twitter.components

import androidx.compose.ui.text.TextLayoutResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class GhostEffectCoordinator(private val scope: CoroutineScope) {

    private val _ghostEvents = MutableSharedFlow<GhostEvent>(extraBufferCapacity = 64)
    val ghostEvents: SharedFlow<GhostEvent> = _ghostEvents.asSharedFlow()

    fun handleTextChange(
        oldText: String,
        newText: String,
        layout: TextLayoutResult?
    ) {
        if (layout == null || oldText.isEmpty() || newText.length >= oldText.length) return

        // Ensure layout matches old text length
        if (layout.layoutInput.text.length != oldText.length) return

        val deletedCount = oldText.length - newText.length
        val startIndex = newText.length

        for (i in 0 until deletedCount) {
            val charIndex = startIndex + i
            if (charIndex >= oldText.length || charIndex >= layout.layoutInput.text.length) break

            val char = oldText[charIndex]
            val box = layout.getBoundingBox(charIndex)

            val seed = GhostSeed(
                id = System.nanoTime(),
                char = char,
                baseX = box.left,
                baseY = box.top,
                order = i
            )

            scope.launch {
                _ghostEvents.emit(GhostEvent.Backspace(seed, delayMs = i * 18L))
            }
        }
    }

    fun handleClear(text: String, layout: TextLayoutResult?) {
        if (layout == null || text.isEmpty()) return

        val total = text.length
        val layoutLength = layout.layoutInput.text.length

        // If layout doesn't match, skip this effect
        if (layoutLength != total) return

        val indices = (0 until total).toList().reversed()
        val smartDelay = when {
            total > 30 -> 3L
            total > 20 -> 5L
            total > 10 -> 8L
            else -> 12L
        }

        val seeds = indices.mapIndexed { order, charIndex ->
            // Safely get character bounds
            if (charIndex >= text.length || charIndex >= layoutLength) {
                return@mapIndexed null
            }
            try {
                val char = text[charIndex]
                val box = layout.getBoundingBox(charIndex)
                GhostSeed(
                    id = System.nanoTime(),
                    char = char,
                    baseX = box.left,
                    baseY = box.top,
                    order = order
                )
            } catch (e: Exception) {
                null
            }
        }.filterNotNull()

        if (seeds.isEmpty()) return

        scope.launch {
            _ghostEvents.emit(GhostEvent.Clear(seeds = seeds, smartDelayMs = smartDelay))
        }
    }
}
