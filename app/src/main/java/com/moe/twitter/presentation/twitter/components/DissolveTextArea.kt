package com.moe.twitter.presentation.twitter.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moe.twitter.presentation.twitter.TwitterState
import com.moe.twitter.ui.theme.twitterColors
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.lang.StrictMath.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun DissolveTextArea(
    modifier: Modifier = Modifier,
    state: TwitterState,
    ghostCoordinator: GhostEffectCoordinator,
    onTextChange: (String) -> Unit,
    onClear: () -> Unit
) {
    val previousText = remember { androidx.compose.runtime.mutableStateOf("") }
    val currentLayout = remember { androidx.compose.runtime.mutableStateOf<TextLayoutResult?>(null) }
    val previousLayout = remember { androidx.compose.runtime.mutableStateOf<TextLayoutResult?>(null) }

    val twitterColor = MaterialTheme.twitterColors
    val maxChars = 280
    val overflowAndErrorsTransform = remember(state.text, state.errors, maxChars) {
        VisualTransformation { original: AnnotatedString ->
            val styled = AnnotatedString.Builder().apply {
                append(original)

                if (original.text.length > maxChars) {
                    addStyle(
                        style = SpanStyle(
                            background = twitterColor.ErrorBackground,
                            color = twitterColor.TwitterRed
                        ), start = maxChars, end = original.length
                    )
                }

                state.errors.forEach { err ->
                    val safeStart = err.start.coerceIn(0, original.text.length)
                    val safeEnd = err.end.coerceIn(safeStart, original.text.length)
                    if (safeStart >= safeEnd) return@forEach

                    val color = when (err.issueType) {
                        "grammar", "typographical" -> twitterColor.TwitterRed
                        "style" -> twitterColor.WarningYellow
                        else -> twitterColor.TwitterRed
                    }

                    addStyle(
                        style = SpanStyle(
                            color = color, textDecoration = TextDecoration.Underline
                        ), start = safeStart, end = safeEnd
                    )
                }
            }.toAnnotatedString()

            TransformedText(styled, OffsetMapping.Identity)
        }
    }

    val ghosts = remember { mutableStateListOf<GhostRender>() }

    val handleTextChangeWithGhost: (String) -> Unit = { newText: String ->
        val oldText = previousText.value
        val layout = currentLayout.value

        // Trigger ghost effects BEFORE updating state
        // ONLY use handleTextChange for user typing/backspace (never handleClear here)
        if (layout != null && oldText.isNotEmpty() && newText.length < oldText.length) {
            ghostCoordinator.handleTextChange(oldText, newText, layout)
        }

        previousText.value = newText
        onTextChange(newText)
    }

    // Sync previousText when state.text changes externally (e.g., from clear button)
    LaunchedEffect(state.text) {
        // Only update if coming from external source (clear button)
        if (state.text != previousText.value) {
            if (state.text.isEmpty() && previousText.value.isNotEmpty()) {
                // Use previousLayout because currentLayout might already be updated
                val layout = previousLayout.value
                if (layout != null) {
                    ghostCoordinator.handleClear(previousText.value, layout)
                }
            }
            previousText.value = state.text
        }
    }

    LaunchedEffect(ghostCoordinator) {
        ghostCoordinator.ghostEvents.collect { event ->
            when (event) {
                is GhostEvent.Backspace -> {
                    launch {
                        delay(event.delayMs)
                        val render = GhostRender.fromSeed(event.seed)
                        ghosts.add(render)
                        animateBackspaceGhost(render, ghosts)
                    }
                }

                is GhostEvent.Clear -> {
                    event.seeds.forEach { seed ->
                        launch {
                            delay(seed.order * event.smartDelayMs)
                            val render = GhostRender.fromSeed(seed)
                            ghosts.add(render)
                            animateExplosionGhost(render, ghosts)
                        }
                    }
                }
            }
        }
    }

    // rgba(6, 26, 64, 0.04)
    val shadowColor = Color(red = 6, green = 26, blue = 64, alpha = (0.4f * 255).toInt())
    Box(
        modifier = modifier
            .background(MaterialTheme.twitterColors.Surface, RoundedCornerShape(12.dp))
            .shadow(
                elevation = 24.dp,              // big, soft-ish
                shape = RoundedCornerShape(12.dp),
                clip = false,
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .background(
                MaterialTheme.twitterColors.Surface,
                RoundedCornerShape(12.dp)
            ) // then the white card
            .border(
                width = 1.dp,
                color = MaterialTheme.twitterColors.BorderGray,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        BasicTextField(
            value = state.text,
            onValueChange = handleTextChangeWithGhost,
            modifier = Modifier
                .fillMaxWidth()
                .height(188.dp),
            textStyle = TextStyle(
                fontSize = 16.sp, color = MaterialTheme.twitterColors.TextDark, lineHeight = 24.sp
            ),
            cursorBrush = SolidColor(MaterialTheme.twitterColors.TwitterBlue),
            visualTransformation = overflowAndErrorsTransform,
            onTextLayout = { newLayout ->
                // Save current as previous before updating
                previousLayout.value = currentLayout.value
                currentLayout.value = newLayout
            }
        )

        if (state.text.isEmpty()) {
            Text(
                text = "Start typing! You can enter up to 280 characters",
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = MaterialTheme.twitterColors.TextSecondary
            )

        }

        GhostLayer(ghosts = ghosts.map { it.toUi() })
    }
}

private data class GhostRender(
    val id: Long,
    val char: Char,
    val baseX: Float,
    val baseY: Float,
    val offsetX: Animatable<Float, AnimationVector1D>,
    val offsetY: Animatable<Float, AnimationVector1D>,
    val alpha: Animatable<Float, AnimationVector1D>,
    val scale: Animatable<Float, AnimationVector1D>,
    val rotation: Animatable<Float, AnimationVector1D>
) {
    fun toUi(): GhostCharUi = GhostCharUi(
        id = id,
        char = char,
        baseX = baseX,
        baseY = baseY,
        offsetX = offsetX.value,
        offsetY = offsetY.value,
        alpha = alpha.value,
        scale = scale.value,
        rotation = rotation.value
    )

    companion object {
        fun fromSeed(seed: GhostSeed): GhostRender = GhostRender(
            id = seed.id,
            char = seed.char,
            baseX = seed.baseX,
            baseY = seed.baseY,
            offsetX = Animatable(0f),
            offsetY = Animatable(0f),
            alpha = Animatable(1f),
            scale = Animatable(1f),
            rotation = Animatable(0f)
        )
    }
}

private suspend fun animateBackspaceGhost(
    ghost: GhostRender, list: MutableList<GhostRender>
) {
    coroutineScope {
        launch {
            ghost.scale.animateTo(0.85f, tween(60, easing = EaseInCubic))
            ghost.scale.animateTo(0.2f, tween(180, easing = EaseInCubic))
        }
        launch {
            ghost.offsetX.animateTo(
                Random.nextInt(120, 180).toFloat(), tween(220, easing = EaseInCubic)
            )
        }
        launch {
            ghost.offsetY.animateTo(
                Random.nextInt(-150, -110).toFloat(), tween(220, easing = EaseInOutCubic)
            )
        }
        launch {
            ghost.alpha.animateTo(0f, tween(200, easing = EaseInCubic))
        }
        launch {
            ghost.rotation.animateTo(
                Random.nextInt(60, 90).toFloat(), tween(220, easing = EaseInOutCubic)
            )
        }
    }
    delay(40)
    list.remove(ghost)
}

private suspend fun animateExplosionGhost(
    ghost: GhostRender, list: MutableList<GhostRender>
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
                targetX, tween(350, easing = EaseOutCubic)
            )
        }
        launch {
            ghost.offsetY.animateTo(
                targetY, tween(350, easing = EaseOutCubic)
            )
        }
        launch {
            ghost.scale.animateTo(1.12f, tween(80, easing = EaseOutCubic))
            ghost.scale.animateTo(1.0f, tween(80, easing = EaseInOutCubic))
            ghost.scale.animateTo(
                0.25f, tween(220, easing = EaseInCubic)
            )
        }
        launch {
            ghost.alpha.animateTo(0.85f, tween(80))
            ghost.alpha.animateTo(0.5f, tween(140))
            ghost.alpha.animateTo(0f, tween(200))
        }
        launch {
            ghost.rotation.animateTo(
                midRot, tween(90, easing = EaseOutCubic)
            )
            ghost.rotation.animateTo(
                finalRot, tween(260, easing = EaseInOutCubic)
            )
        }
    }
    delay(60)
    list.remove(ghost)
}
