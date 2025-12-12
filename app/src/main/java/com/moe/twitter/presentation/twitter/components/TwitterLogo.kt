package com.moe.twitter.presentation.twitter.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieAnimatable
import com.airbnb.lottie.compose.rememberLottieComposition
import com.moe.twitter.R
import com.moe.twitter.presentation.twitter.PostingState
import com.moe.twitter.ui.theme.TwitterCounterTheme
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin

@Composable
fun TwitterLogo(
    modifier: Modifier = Modifier,
    postingState: PostingState,
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.twitter))
    val animatable = rememberLottieAnimatable()

    // Flight progress: 0f = center, 1f = corner
    val flightProgress = remember { Animatable(0f) }

    // Shake for error
    val shakeX = remember { Animatable(0f) }


    // --- Handle winging (Lottie) based on posting state ---
    LaunchedEffect(postingState, composition) {
        val comp = composition ?: return@LaunchedEffect

        when (postingState) {
            PostingState.Idle -> {
                animatable.snapTo(comp, 0f)
            }

            PostingState.Posting, PostingState.Success -> {
                // Keep winging forever while posting or after success
                animatable.animate(
                    composition = comp, iterations = LottieConstants.IterateForever, speed = 1.0f
                )
            }

            is PostingState.Error -> {
                // Freeze wings; shaking handles motion
                animatable.snapTo(comp, 0f)
            }
        }
    }

    // --- Handle flight behavior for sent animation ---
    LaunchedEffect(postingState) {
        when (postingState) {
            PostingState.Success -> {
                // Fly out
                flightProgress.animateTo(
                    targetValue = 1f, animationSpec = tween(
                        durationMillis = 1000, easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
                    )
                )
                // Stay out in the corner
                delay(800)
                // Fly back home
                flightProgress.animateTo(
                    targetValue = 0f, animationSpec = tween(
                        durationMillis = 1000, easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
                    )
                )
            }

            PostingState.Idle, PostingState.Posting -> {
                // Always ensure it eventually comes back center
                flightProgress.animateTo(
                    targetValue = 0f, animationSpec = tween(
                        durationMillis = 700, easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
                    )
                )
            }

            is PostingState.Error -> {
                // No flight; just ensure it's centered
                flightProgress.snapTo(0f)
            }
        }
    }

    // --- Error shake (one-shot burst when entering error) ---
    LaunchedEffect(postingState) {
        if (postingState is PostingState.Error) {
            val duration = 300L
            val start = System.currentTimeMillis()
            while (System.currentTimeMillis() - start < duration) {
                val t = (System.currentTimeMillis() - start).toFloat() / duration
                val value = (sin(t * PI * 8) * 8f * (1f - t)).toFloat()
                shakeX.snapTo(value)
                delay(16)
            }
            shakeX.snapTo(0f)
        } else {
            shakeX.snapTo(0f)
        }
    }

    // --- Derived transforms from flightProgress ---

    val p = flightProgress.value

    // X offset: move to the right with ease-out
    val offsetXDp = run {
        val eased = 1f - (1f - p).pow(3)
        (eased * 120f).dp
    }

    // Y offset: a little arc + lift
    val offsetYDp = run {
        val arcHeight = -30f
        val arcComponent = if (p < 0.5f) {
            4 * arcHeight * p * (1 - p * 2)
        } else {
            arcHeight * (1 - p) * 2
        }
        val easedY = 1f - (1f - p).pow(3)
        (arcComponent + (easedY * -200f)).dp
    }

    // Scale: shrink when far away
    val scale = run {
        val inv = 1f - p
        0.4f + (inv.pow(2) * 0.6f) // 1 → 0.4
    }

    // Rotation: a little tilt
    val rotation = p * 15f

    // Alpha: slightly fade when flying away
    val alpha = 0.5f + (1f - p) * 0.5f

    val density = LocalDensity.current
    val translationX = with(density) { offsetXDp.toPx() } + shakeX.value
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

// ---------------------------------------------------------------------
// Simple state previews
// ---------------------------------------------------------------------

@Preview(name = "Logo - Idle")
@Composable
private fun TwitterLogoIdlePreview() {
    TwitterCounterTheme {
        Box(modifier = Modifier.size(96.dp)) {
            TwitterLogo(
                modifier = Modifier.size(72.dp), postingState = PostingState.Idle
            )
        }
    }
}

@Preview(name = "Logo - Posting")
@Composable
private fun TwitterLogoPostingPreview() {
    TwitterCounterTheme {
        Box(modifier = Modifier.size(96.dp)) {
            TwitterLogo(
                modifier = Modifier.size(72.dp), postingState = PostingState.Posting
            )
        }
    }
}

@Preview(name = "Logo - Success")
@Composable
private fun TwitterLogoSuccessPreview() {
    TwitterCounterTheme {
        Box(modifier = Modifier.size(96.dp)) {
            TwitterLogo(
                modifier = Modifier.size(72.dp), postingState = PostingState.Success
            )
        }
    }
}

@Preview(name = "Logo - Error")
@Composable
private fun TwitterLogoErrorPreview() {
    TwitterCounterTheme {
        Box(modifier = Modifier.size(96.dp)) {
            TwitterLogo(
                modifier = Modifier.size(72.dp), postingState = PostingState.Error("Oops")
            )
        }
    }
}

// ---------------------------------------------------------------------
// Scenario previews: full flow simulations
// ---------------------------------------------------------------------

@Preview(name = "Flow - Success Scenario", showBackground = true)
@Composable
private fun TwitterLogoSuccessFlowPreview() {
    TwitterCounterTheme {
        var state by remember { mutableStateOf<PostingState>(PostingState.Idle) }

        LaunchedEffect(Unit) {
            // Simulate: Idle -> Posting -> Success -> Idle loop
            while (true) {
                state = PostingState.Idle
                delay(800)

                state = PostingState.Posting
                delay(1200)

                state = PostingState.Success
                delay(3000)

                state = PostingState.Idle
                delay(1500)
            }
        }

        Box(modifier = Modifier.size(120.dp)) {
            TwitterLogo(
                modifier = Modifier.size(72.dp), postingState = state
            )
        }
    }
}

@Preview(name = "Flow - Error Scenario", showBackground = true)
@Composable
private fun TwitterLogoErrorFlowPreview() {
    TwitterCounterTheme {
        var state by remember { mutableStateOf<PostingState>(PostingState.Idle) }

        LaunchedEffect(Unit) {
            // Simulate: Idle -> Posting -> Error -> Idle loop
            while (true) {
                state = PostingState.Idle
                delay(800)

                state = PostingState.Posting
                delay(1200)

                state = PostingState.Error("Network error")
                delay(1800)

                state = PostingState.Idle
                delay(1500)
            }
        }

        Box(modifier = Modifier.size(120.dp)) {
            TwitterLogo(
                modifier = Modifier.size(72.dp), postingState = state
            )
        }
    }
}
