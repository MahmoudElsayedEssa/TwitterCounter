package com.moe.twitter.presentation.twitter.components.morph

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import kotlin.math.pow

/**
 * Configuration for elegant text morphing with minimal, refined animations.
 */
data class MorphConfig(
    val duration: Int = 400,
    val staggerDelay: Int = 25,
    val maxScale: Float = 1.08f,
    val minAlpha: Float = 0f
)

/**
 * Represents a character position in the morph transition.
 */
private sealed class CharacterState {
    data class Stable(val char: Char) : CharacterState()
    data class Appearing(val char: Char, val delay: Int) : CharacterState()
    data class Disappearing(val char: Char, val delay: Int) : CharacterState()
    data class Morphing(val from: Char, val to: Char, val delay: Int) : CharacterState()
    object Empty : CharacterState()
}

/**
 * Text transition using LCS algorithm to minimize morphing.
 * Only characters that actually change will animate, creating a refined, minimal effect.
 */
@Composable
fun TextTransition(
    targetText: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    color: Color = Color.White,
    config: MorphConfig = MorphConfig()
) {
    val previousText = remember { androidx.compose.runtime.mutableStateOf(targetText) }
    val isAnimating = remember { androidx.compose.runtime.mutableStateOf(false) }

    // Compute character states using smart diffing
    val characterStates = remember(targetText) {
        if (previousText.value == targetText) {
            targetText.map { CharacterState.Stable(it) }
        } else {
            computeDiff(previousText.value, targetText, config.staggerDelay)
        }
    }

    LaunchedEffect(targetText) {
        if (previousText.value != targetText) {
            isAnimating.value = true
            kotlinx.coroutines.delay((config.duration + config.staggerDelay * characterStates.size).toLong())
            isAnimating.value = false
            previousText.value = targetText
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        characterStates.forEach { state ->
            when (state) {
                is CharacterState.Stable -> {
                    Text(
                        text = state.char.toString(),
                        style = textStyle,
                        color = color,
                        textAlign = TextAlign.Center
                    )
                }
                is CharacterState.Appearing -> {
                    AppearingCharacter(
                        char = state.char,
                        isAnimating = isAnimating.value,
                        delay = state.delay,
                        textStyle = textStyle,
                        color = color,
                        config = config
                    )
                }
                is CharacterState.Disappearing -> {
                    DisappearingCharacter(
                        char = state.char,
                        isAnimating = isAnimating.value,
                        delay = state.delay,
                        textStyle = textStyle,
                        color = color,
                        config = config
                    )
                }
                is CharacterState.Morphing -> {
                    MorphingCharacter(
                        fromChar = state.from,
                        toChar = state.to,
                        isAnimating = isAnimating.value,
                        delay = state.delay,
                        textStyle = textStyle,
                        color = color,
                        config = config
                    )
                }
                is CharacterState.Empty -> {
                    // Placeholder for layout stability
                    Text(
                        text = " ",
                        style = textStyle,
                        color = Color.Transparent,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Uses a smart diffing algorithm to determine which characters should animate.
 * This creates minimal, elegant transitions by keeping common characters stable.
 */
private fun computeDiff(
    from: String,
    to: String,
    baseDelay: Int
): List<CharacterState> {
    if (from.isEmpty()) {
        return to.mapIndexed { index, char ->
            CharacterState.Appearing(char, index * baseDelay)
        }
    }

    if (to.isEmpty()) {
        return from.mapIndexed { index, char ->
            CharacterState.Disappearing(char, index * baseDelay)
        }
    }

    // Use LCS-based diffing for elegant transitions
    val lcs = longestCommonSubsequence(from, to)
    val result = mutableListOf<CharacterState>()

    var fromIndex = 0
    var toIndex = 0
    var lcsIndex = 0
    var delayCounter = 0

    while (toIndex < to.length || fromIndex < from.length) {
        when {
            // Character is in LCS - keep it stable
            lcsIndex < lcs.length &&
            fromIndex < from.length &&
            toIndex < to.length &&
            from[fromIndex] == to[toIndex] &&
            from[fromIndex] == lcs[lcsIndex] -> {
                result.add(CharacterState.Stable(to[toIndex]))
                fromIndex++
                toIndex++
                lcsIndex++
            }
            // Character changes - morph it
            fromIndex < from.length && toIndex < to.length -> {
                result.add(CharacterState.Morphing(
                    from[fromIndex],
                    to[toIndex],
                    delayCounter * baseDelay
                ))
                fromIndex++
                toIndex++
                delayCounter++
            }
            // Only in target - appear
            toIndex < to.length -> {
                result.add(CharacterState.Appearing(
                    to[toIndex],
                    delayCounter * baseDelay
                ))
                toIndex++
                delayCounter++
            }
            // Only in source - disappear
            else -> {
                result.add(CharacterState.Disappearing(
                    from[fromIndex],
                    delayCounter * baseDelay
                ))
                fromIndex++
                delayCounter++
            }
        }
    }

    return result
}

/**
 * Longest Common Subsequence algorithm for finding stable characters.
 */
private fun longestCommonSubsequence(s1: String, s2: String): String {
    val m = s1.length
    val n = s2.length
    val dp = Array(m + 1) { IntArray(n + 1) }

    for (i in 1..m) {
        for (j in 1..n) {
            dp[i][j] = if (s1[i - 1] == s2[j - 1]) {
                dp[i - 1][j - 1] + 1
            } else {
                maxOf(dp[i - 1][j], dp[i][j - 1])
            }
        }
    }

    // Backtrack to build LCS
    val lcs = StringBuilder()
    var i = m
    var j = n
    while (i > 0 && j > 0) {
        when {
            s1[i - 1] == s2[j - 1] -> {
                lcs.append(s1[i - 1])
                i--
                j--
            }
            dp[i - 1][j] > dp[i][j - 1] -> i--
            else -> j--
        }
    }

    return lcs.reverse().toString()
}

/**
 * Character that appears with elegant fade-in and scale.
 */
@Composable
private fun AppearingCharacter(
    char: Char,
    isAnimating: Boolean,
    delay: Int,
    textStyle: TextStyle,
    color: Color,
    config: MorphConfig
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            progress.snapTo(0f)
            kotlinx.coroutines.delay(delay.toLong())
            progress.animateTo(1f, animationSpec = tween(config.duration))
        }
    }

    val alpha = easeOutCubic(progress.value)
    val scale = config.minAlpha + (1f - config.minAlpha) * easeOutBack(progress.value)

    Box(contentAlignment = Alignment.Center) {
        Text(
            text = char.toString(),
            style = textStyle,
            color = color,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .alpha(alpha)
                .scale(scale)
        )
    }
}

/**
 * Character that disappears with elegant fade-out and scale.
 */
@Composable
private fun DisappearingCharacter(
    char: Char,
    isAnimating: Boolean,
    delay: Int,
    textStyle: TextStyle,
    color: Color,
    config: MorphConfig
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            progress.snapTo(0f)
            kotlinx.coroutines.delay(delay.toLong())
            progress.animateTo(1f, animationSpec = tween(config.duration))
        }
    }

    val alpha = 1f - easeInCubic(progress.value)
    val scale = 1f - (1f - config.minAlpha) * easeInBack(progress.value)

    if (alpha > 0.01f) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = char.toString(),
                style = textStyle,
                color = color,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(alpha)
                    .scale(scale)
            )
        }
    }
}

/**
 * Character that morphs from one to another with elegant crossfade.
 */
@Composable
private fun MorphingCharacter(
    fromChar: Char,
    toChar: Char,
    isAnimating: Boolean,
    delay: Int,
    textStyle: TextStyle,
    color: Color,
    config: MorphConfig
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            progress.snapTo(0f)
            kotlinx.coroutines.delay(delay.toLong())
            progress.animateTo(1f, animationSpec = tween(config.duration))
        }
    }

    // Smooth crossfade with elegant scaling
    val outgoingAlpha = 1f - easeInQuad(progress.value)
    val incomingAlpha = easeOutQuad(progress.value)

    val outgoingScale = 1f - 0.1f * easeInQuad(progress.value)
    val incomingScale = 0.9f + 0.1f * easeOutQuad(progress.value)

    Box(contentAlignment = Alignment.Center) {
        // Outgoing character
        if (outgoingAlpha > 0.01f) {
            Text(
                text = fromChar.toString(),
                style = textStyle,
                color = color,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(outgoingAlpha)
                    .scale(outgoingScale)
            )
        }

        // Incoming character
        Text(
            text = toChar.toString(),
            style = textStyle,
            color = color,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .alpha(incomingAlpha)
                .scale(incomingScale)
        )
    }
}

// Elegant easing functions using mathematical curves
private fun easeOutCubic(t: Float): Float = 1f - (1f - t).pow(3)
private fun easeInCubic(t: Float): Float = t.pow(3)
private fun easeOutQuad(t: Float): Float = 1f - (1f - t).pow(2)
private fun easeInQuad(t: Float): Float = t.pow(2)

private fun easeOutBack(t: Float): Float {
    val c1 = 1.70158f
    val c3 = c1 + 1f
    return 1f + c3 * (t - 1f).pow(3) + c1 * (t - 1f).pow(2)
}

private fun easeInBack(t: Float): Float {
    val c1 = 1.70158f
    val c3 = c1 + 1f
    return c3 * t.pow(3) - c1 * t.pow(2)
}
