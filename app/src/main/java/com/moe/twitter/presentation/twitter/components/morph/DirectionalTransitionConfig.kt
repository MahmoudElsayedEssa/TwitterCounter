package com.moe.twitter.presentation.twitter.components.morph

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Configuration for the DirectionalTextTransition effect.
 *
 * @param transitionDuration Duration of the transition animation in milliseconds.
 * @param staggerDelay Base delay between character animations in milliseconds.
 * @param staggerFactor Multiplier for stagger delay between subsequent characters.
 * @param trailCount Number of ghost trail instances for each character.
 * @param maxVerticalOffset Maximum vertical movement distance during animation.
 * @param maxHorizontalOffset Maximum horizontal sway during animation.
 * @param maxBlur Maximum blur amount applied to trailing instances.
 * @param rotationAmount Maximum rotation angle in degrees.
 * @param scaleRange Pair of (minimum, maximum) scale values during transition.
 * @param bunching Power factor for trail bunching (higher = more bunched).
 * @param easingCurve Easing curve for the animation.
 */
data class DirectionalTransitionConfig(
    val transitionDuration: Int = 600,
    val staggerDelay: Long = 40,
    val staggerFactor: Float = 0.8f,
    val trailCount: Int = 6,
    val maxVerticalOffset: Dp = 32.dp,
    val maxHorizontalOffset: Dp = 2.dp,
    val maxBlur: Dp = 8.dp,
    val rotationAmount: Float = 4f,
    val scaleRange: Pair<Float, Float> = Pair(0.7f, 1.1f),
    val bunching: Float = 2.5f,
    val easingCurve: Easing = FastOutSlowInEasing
)

/**
 * Direction of the transition animation.
 */
enum class TransitionDirection {
    FORWARD, BACKWARD
}
