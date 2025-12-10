package com.moe.twitter.presentation.twitter.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieAnimatable
import com.airbnb.lottie.compose.rememberLottieComposition
import com.moe.twitter.R
import kotlinx.coroutines.delay
import kotlin.math.pow

@Composable
fun TwitterLogo(
    trigger: Int,
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.twitter))
    val animatable = rememberLottieAnimatable()

    var flyState by remember { mutableStateOf(FlyState.Center) }

    // Smooth easing with custom bezier curve for natural motion
    val animationProgress by animateFloatAsState(
        targetValue = when (flyState) {
            FlyState.Center -> 0f
            FlyState.Corner -> 1f
        },
        animationSpec = tween(
            durationMillis = 1800,
            easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f) // Smooth acceleration/deceleration
        )
    )

    // Calculate offset using parabolic arc for natural flight path
    val offsetX = remember(animationProgress) {
        val easeProgress = animationProgress
        // Ease out cubic for smooth deceleration
        val easedX = 1f - (1f - easeProgress).pow(3)
        (easedX * 120).dp
    }

    val offsetY = remember(animationProgress) {
        // Create a subtle arc trajectory (parabola)
        val arcHeight = -30f // Slight upward arc
        val progress = animationProgress

        // Parabolic motion: y = 4h * x(1-x) for arc, then continue down
        val arcComponent = if (progress < 0.5f) {
            4 * arcHeight * progress * (1 - progress * 2)
        } else {
            arcHeight * (1 - progress) * 2
        }

        // Ease out for smooth deceleration
        val easedY = 1f - (1f - progress).pow(3)
        (arcComponent + (easedY * -200)).dp
    }

    // Scale with ease out for smooth shrinking
    val scale = remember(animationProgress) {
        val invProgress = 1f - animationProgress
        0.4f + (invProgress.pow(2) * 0.6f) // From 1.0 to 0.4
    }

    // Subtle rotation for more dynamic feel
    val rotation = remember(animationProgress) {
        animationProgress * 15f // Gentle 15-degree rotation
    }

    // Alpha fade for elegance
    val alpha = remember(animationProgress) {
        0.5f + (1f - animationProgress) * 0.5f // From 1.0 to 0.5
    }

    LaunchedEffect(trigger) {
        if (composition != null && trigger > 0) {
            // Play the Lottie animation
            animatable.animate(
                composition = composition,
                iterations = 1,
                speed = 1.2f
            )
            // Fly out then return
            delay(400)
            flyState = FlyState.Corner
            delay(1800)
            flyState = FlyState.Center
        }
    }

    LottieAnimation(
        composition = composition,
        progress = { animatable.progress },
        modifier = modifier
            .graphicsLayer(
                translationX = offsetX.value,
                translationY = offsetY.value,
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