package com.moe.twitter.presentation.twitter.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moe.twitter.ui.theme.TwitterCounterTheme
import com.moe.twitter.ui.theme.twitterColors
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sin

@Composable
fun AnimatedNumber(
    modifier: Modifier = Modifier,
    value: String,
    style: TextStyle = LocalTextStyle.current.copy(fontSize = 20.sp)
) {
    val animationState = remember { NumberAnimationState() }

    LaunchedEffect(value) {
        val newNum = value.toIntOrNull()
        val oldNum = animationState.previousValue.toIntOrNull()

        animationState.direction = when {
            newNum != null && oldNum != null && newNum > oldNum -> Direction.Up
            newNum != null && oldNum != null && newNum < oldNum -> Direction.Down
            else -> Direction.None
        }

        animationState.changeAmount = if (newNum != null && oldNum != null) {
            abs(newNum - oldNum)
        } else 0

        animationState.previousValue = value
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        value.forEachIndexed { index, char ->
            val digit = remember(char, index) {
                NumberDigit(character = char, position = index)
            }

            AnimatedContent(
                targetState = digit,
                transitionSpec = {
                    val slideDirection = when (animationState.direction) {
                        Direction.Up -> -1
                        Direction.Down -> 1
                        Direction.None -> 0
                    }

                    if (targetState.character.isDigit() && slideDirection != 0) {
                        (slideInVertically { it * slideDirection } + fadeIn(tween(250))) togetherWith
                                (slideOutVertically { -it * slideDirection } + fadeOut(tween(250)))
                    } else {
                        fadeIn(tween(150)) togetherWith fadeOut(tween(150))
                    }.using(SizeTransform(clip = false))
                },
                label = "digit_animation_$index"
            ) { animatedDigit ->
                DigitWithPhysics(
                    digit = animatedDigit,
                    direction = animationState.direction,
                    changeAmount = animationState.changeAmount,
                    textStyle = style
                )
            }
        }
    }
}

@Composable
private fun DigitWithPhysics(
    digit: NumberDigit,
    direction: Direction,
    changeAmount: Int,
    textStyle: TextStyle
) {
    val physicsState = remember { PhysicsState() }

    LaunchedEffect(digit.character) {
        if (direction == Direction.None) return@LaunchedEffect

        val effectIntensity = (changeAmount.toFloat() / 10f).coerceIn(0.3f, 1.5f)

        physicsState.cancelAllAnimations()

        physicsState.blur.snapTo(8f * effectIntensity)
        physicsState.glowIntensity.snapTo(0.6f * effectIntensity)

        physicsState.scaleAnimation = launch {
            physicsState.scale.snapTo(0.8f)
            physicsState.scale.animateTo(
                targetValue = 1.01f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            delay(200)
            physicsState.scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        physicsState.rotationAnimation = launch {
            animateRotationWobble(
                animatable = physicsState.rotation,
                amplitude = 12f * effectIntensity,
                direction = direction,
                wobbleFrequency = 2.5,
                durationMillis = 400L
            )
        }

        physicsState.horizontalShakeAnimation = launch {
            animateHorizontalShake(
                animatable = physicsState.horizontalOffset,
                amplitude = 6f * effectIntensity,
                shakeFrequency = 4.0,
                durationMillis = 300L
            )
        }

        physicsState.blurAnimation = launch {
            delay(100)
            physicsState.blur.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 300, easing = EaseInOutCubic)
            )
        }

        physicsState.glowAnimation = launch {
            delay(50)
            physicsState.glowIntensity.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 350)
            )
        }
    }

    Box(
        modifier = Modifier.padding(horizontal = 1.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = digit.character.toString(),
            style = textStyle,
            modifier = Modifier
                .blur(radius = physicsState.blur.value.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                .graphicsLayer {
                    scaleX = physicsState.scale.value
                    scaleY = physicsState.scale.value
                    rotationZ = physicsState.rotation.value
                    translationX = physicsState.horizontalOffset.value
                }
        )
    }
}

@Stable
private class NumberAnimationState {
    var previousValue by mutableStateOf("")
    var direction by mutableStateOf(Direction.None)
    var changeAmount by mutableIntStateOf(0)
}

@Stable
private class PhysicsState {
    val scale = Animatable(1f)
    val rotation = Animatable(0f)
    val horizontalOffset = Animatable(0f)
    val glowIntensity = Animatable(0f)
    val blur = Animatable(0f)

    var scaleAnimation: Job? = null
    var rotationAnimation: Job? = null
    var horizontalShakeAnimation: Job? = null
    var blurAnimation: Job? = null
    var glowAnimation: Job? = null

    fun cancelAllAnimations() {
        scaleAnimation?.cancel()
        rotationAnimation?.cancel()
        horizontalShakeAnimation?.cancel()
        blurAnimation?.cancel()
        glowAnimation?.cancel()
    }
}

private suspend fun animateRotationWobble(
    animatable: Animatable<Float, AnimationVector1D>,
    amplitude: Float,
    direction: Direction,
    wobbleFrequency: Double,
    durationMillis: Long
) {
    val initialRotation = if (direction == Direction.Up) -amplitude else amplitude
    animatable.snapTo(initialRotation)

    val startTime = System.currentTimeMillis()
    while (System.currentTimeMillis() - startTime < durationMillis) {
        val elapsedTime = System.currentTimeMillis() - startTime
        val progress = elapsedTime / durationMillis.toFloat()
        val wobbleValue = amplitude * sin(progress * PI * wobbleFrequency) * (1 - progress).pow(2)
        animatable.snapTo(wobbleValue.toFloat())
        delay(16)
    }
    animatable.snapTo(0f)
}

private suspend fun animateHorizontalShake(
    animatable: Animatable<Float, AnimationVector1D>,
    amplitude: Float,
    shakeFrequency: Double,
    durationMillis: Long
) {
    val startTime = System.currentTimeMillis()
    while (System.currentTimeMillis() - startTime < durationMillis) {
        val elapsedTime = System.currentTimeMillis() - startTime
        val progress = elapsedTime / durationMillis.toFloat()
        val shakeValue = amplitude * sin(progress * PI * shakeFrequency) * (1 - progress).pow(1.5f)
        animatable.snapTo(shakeValue.toFloat())
        delay(16)
    }
    animatable.snapTo(0f)
}

@Stable
private data class NumberDigit(
    val character: Char,
    val position: Int
)

private enum class Direction {
    Up,
    Down,
    None
}

@Preview(name = "Animated Number - Single Digit")
@Composable
private fun AnimatedNumberSinglePreview() {
    TwitterCounterTheme {
        Surface {
            AnimatedNumber(
                value = "5",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.twitterColors.TextPrimary
                )
            )
        }
    }
}

@Preview(name = "Animated Number - Multiple Digits")
@Composable
private fun AnimatedNumberMultiplePreview() {
    TwitterCounterTheme {
        Surface {
            AnimatedNumber(
                value = "280",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.twitterColors.TextPrimary
                )
            )
        }
    }
}

@Preview(name = "Animated Number - Negative")
@Composable
private fun AnimatedNumberNegativePreview() {
    TwitterCounterTheme {
        Surface {
            AnimatedNumber(
                value = "-15",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.twitterColors.TwitterRed
                )
            )
        }
    }
}