package com.moe.twitter.presentation.twitter.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieAnimatable
import com.airbnb.lottie.compose.rememberLottieComposition
import com.moe.twitter.R
import com.moe.twitter.presentation.twitter.PostingState
import kotlinx.coroutines.delay
import kotlin.math.pow
import kotlin.math.sin

@Composable
fun TwitterLogo(
    modifier: Modifier = Modifier,
    postingState: PostingState,
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.twitter))
    val animatable = rememberLottieAnimatable()

    var flyState by remember { mutableStateOf(FlyState.Center) }
    var previousPostingState by remember { mutableStateOf<PostingState>(PostingState.Idle) }

    // Smooth flight progress between center <-> corner
    val animationProgress by animateFloatAsState(
        targetValue = when (flyState) {
            FlyState.Center -> 0f
            FlyState.Corner -> 1f
        },
        animationSpec = tween(
            durationMillis = 1000,
            easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
        ),
        label = "flight_progress"
    )

    // --- Flight path in DP (we'll convert to PX later) ---

    // X: smooth ease-out to the right
    val offsetXDp = run {
        val easeProgress = animationProgress
        val easedX = 1f - (1f - easeProgress).pow(3)
        (easedX * 120f).dp
    }

    // Y: simple upward arc then drop
    val offsetYDp = run {
        val arcHeight = -30f // upward
        val progress = animationProgress

        val arcComponent = if (progress < 0.5f) {
            // first half: go up
            4 * arcHeight * progress * (1 - progress * 2)
        } else {
            // second half: start going down
            arcHeight * (1 - progress) * 2
        }

        val easedY = 1f - (1f - progress).pow(3)
        (arcComponent + (easedY * -200f)).dp
    }

    // Scale: shrink a bit as it flies away
    val scale = run {
        val invProgress = 1f - animationProgress
        0.4f + (invProgress.pow(2) * 0.6f) // 1.0 -> 0.4
    }

    // Small rotation while flying
    val rotation = animationProgress * 15f

    // Slight alpha fade (1f -> 0.5f)
    val alpha = 0.5f + (1f - animationProgress) * 0.5f

    // --- Shake animation for error state ---

    val infiniteTransition = rememberInfiniteTransition(label = "shake")
    val shakeOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(100),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake_offset"
    )

    val shakeXPx = if (postingState is PostingState.Error) {
        (sin(shakeOffset * Math.PI * 8) * 8f).toFloat() // ±8 px
    } else {
        0f
    }

    // --- Drive Lottie state based on postingState ---

    LaunchedEffect(postingState, composition) {
        if (composition == null) return@LaunchedEffect

        when (postingState) {
            PostingState.Idle -> {
                flyState = FlyState.Center
                // reset to first frame
                animatable.snapTo(composition, 0f)
            }

            PostingState.Posting -> {
                // Stay in center, just keep winging
                flyState = FlyState.Center
                if (previousPostingState != PostingState.Posting) {
                    animatable.animate(
                        composition = composition,
                        iterations = LottieConstants.IterateForever,
                        speed = 1.0f
                    )
                }
            }

            PostingState.Success -> {
                if (previousPostingState != PostingState.Success) {
                    // Wing once, then fly to corner and back
                    animatable.animate(
                        composition = composition,
                        iterations = 1,
                        speed = 1.2f
                    )
                    // start flight
                    flyState = FlyState.Corner
                    delay(1800)
                    // return to center after celebration
                    flyState = FlyState.Center
                }
            }

            is PostingState.Error -> {
                // Freeze wings, shaking is handled in transform
                if (previousPostingState !is PostingState.Error) {
                    animatable.snapTo(composition, 0f)
                    flyState = FlyState.Center
                }
            }
        }

        previousPostingState = postingState
    }

    // --- Convert DP offsets to PX for graphicsLayer ---

    val density = LocalDensity.current
    val translationX = with(density) { offsetXDp.toPx() } + shakeXPx
    val translationY = with(density) { offsetYDp.toPx() }

    LottieAnimation(
        composition = composition,
        progress = { animatable.progress },
        modifier = modifier.graphicsLayer(
            translationX = translationX,
            translationY = translationY,
            scaleX = scale,
            scaleY = scale,
            rotationZ = rotation,
            alpha = alpha
        )
    )
}

private enum class FlyState {
    Center, Corner
}
