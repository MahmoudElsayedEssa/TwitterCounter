package com.moe.twitter.presentation.twitter.components.chars_ghosting

import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.text.TextLayoutResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.StrictMath.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class GhostController(
    private val ghosts: MutableList<GhostRender>,
    private val currentLayoutState: MutableState<TextLayoutResult?>,
    private val previousLayoutState: MutableState<TextLayoutResult?>,
    private val scope: CoroutineScope
) {

    val ghostsUi: List<GhostCharUi>
        get() = ghosts.map { it.toUi() }

    fun onLayout(newLayout: TextLayoutResult) {
        previousLayoutState.value = currentLayoutState.value
        currentLayoutState.value = newLayout
    }

    /**
     * Called on every user text change from the text field.
     */
    fun onIncomingTextChange(
        oldText: String,
        newText: String
    ) {
        val layout = currentLayoutState.value ?: return

        // Backspace -> spawn ghosts for deleted characters
        if (oldText.isNotEmpty() && newText.length < oldText.length) {
            spawnBackspaceGhosts(oldText, newText, layout)
        }
    }

    /**
     * Called when text is cleared externally (e.g. clear draft / reset).
     */
    fun onExternalClear(previousText: String) {
        val layout = previousLayoutState.value ?: return
        if (previousText.isNotEmpty()) {
            spawnExplosionGhosts(previousText, layout)
        }
    }

    private fun spawnBackspaceGhosts(
        oldText: String,
        newText: String,
        layout: TextLayoutResult
    ) {
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
                delay(i * 18L)
                val render = GhostRender.fromSeed(seed)
                ghosts.add(render)
                animateBackspaceGhost(render, ghosts)
            }
        }
    }

    private fun spawnExplosionGhosts(
        text: String,
        layout: TextLayoutResult
    ) {
        if (layout.layoutInput.text.length != text.length) return

        val total = text.length
        val smartDelay = when {
            total > 30 -> 3L
            total > 20 -> 5L
            total > 10 -> 8L
            else -> 12L
        }

        val indices = (0 until total).toList().reversed()
        indices.forEachIndexed { order, charIndex ->
            if (charIndex >= text.length || charIndex >= layout.layoutInput.text.length) return@forEachIndexed

            try {
                val char = text[charIndex]
                val box = layout.getBoundingBox(charIndex)
                val seed = GhostSeed(
                    id = System.nanoTime(),
                    char = char,
                    baseX = box.left,
                    baseY = box.top,
                    order = order
                )

                scope.launch {
                    delay(order * smartDelay)
                    val render = GhostRender.fromSeed(seed)
                    ghosts.add(render)
                    animateExplosionGhost(render, ghosts)
                }
            } catch (_: Exception) {
                // ignore bad indices/layout glitches
            }
        }
    }

   private suspend fun animateBackspaceGhost(
        ghost: GhostRender,
        list: MutableList<GhostRender>
    ) {
        coroutineScope {
            launch {
                ghost.scale.animateTo(0.85f, tween(60, easing = EaseInCubic))
                ghost.scale.animateTo(0.2f, tween(180, easing = EaseInCubic))
            }
            launch {
                ghost.offsetX.animateTo(
                    Random.nextInt(120, 180).toFloat(),
                    tween(220, easing = EaseInCubic)
                )
            }
            launch {
                ghost.offsetY.animateTo(
                    Random.nextInt(-150, -110).toFloat(),
                    tween(220, easing = EaseInOutCubic)
                )
            }
            launch {
                ghost.alpha.animateTo(0f, tween(200, easing = EaseInCubic))
            }
            launch {
                ghost.rotation.animateTo(
                    Random.nextInt(60, 90).toFloat(),
                    tween(220, easing = EaseInOutCubic)
                )
            }
        }
        delay(40)
        list.remove(ghost)
    }


   private suspend fun animateExplosionGhost(
        ghost: GhostRender,
        list: MutableList<GhostRender>
    ) {
        val angle = Random.nextDouble() * 2 * PI
        val distance = Random.nextInt(100, 180).toFloat()
        val targetX = (cos(angle) * distance).toFloat()
        val targetY = (sin(angle) * distance).toFloat() - 50f

        val midRot = Random.nextInt(15, 35).toFloat()
        val finalRot = midRot + Random.nextInt(25, 45)

        coroutineScope {
            launch {
                ghost.offsetX.animateTo(
                    targetX,
                    tween(350, easing = EaseOutCubic)
                )
            }
            launch {
                ghost.offsetY.animateTo(
                    targetY,
                    tween(350, easing = EaseOutCubic)
                )
            }
            launch {
                ghost.scale.animateTo(1.12f, tween(80, easing = EaseOutCubic))
                ghost.scale.animateTo(1.0f, tween(80, easing = EaseInOutCubic))
                ghost.scale.animateTo(
                    0.25f,
                    tween(220, easing = EaseInCubic)
                )
            }
            launch {
                ghost.alpha.animateTo(0.85f, tween(80))
                ghost.alpha.animateTo(0.5f, tween(140))
                ghost.alpha.animateTo(0f, tween(200))
            }
            launch {
                ghost.rotation.animateTo(
                    midRot,
                    tween(90, easing = EaseOutCubic)
                )
                ghost.rotation.animateTo(
                    finalRot,
                    tween(260, easing = EaseInOutCubic)
                )
            }
        }
        delay(60)
        list.remove(ghost)
    }

}


@Composable
fun rememberGhostController(): GhostController {
    val ghosts: SnapshotStateList<GhostRender> = remember { mutableStateListOf() }
    val currentLayout: MutableState<TextLayoutResult?> = remember { mutableStateOf(null) }
    val previousLayout: MutableState<TextLayoutResult?> = remember { mutableStateOf(null) }
    val scope = rememberCoroutineScope()

    return remember {
        GhostController(
            ghosts = ghosts,
            currentLayoutState = currentLayout,
            previousLayoutState = previousLayout,
            scope = scope
        )
    }
}
