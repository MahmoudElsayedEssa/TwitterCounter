package com.moe.twitter.presentation.twitter.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moe.twitter.presentation.twitter.TwitterState
import com.moe.twitter.presentation.twitter.components.morph.MorphConfig
import com.moe.twitter.presentation.twitter.components.morph.TextTransition
import kotlinx.coroutines.delay

private enum class PostButtonState { Idle, Invalid, Loading, Success, Error }

@Composable
fun PostTweetButton(
    state: TwitterState,
    onPost: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isInvalid = state.text.isBlank() || !state.metrics.withinLimit

    var buttonState by remember { mutableStateOf(PostButtonState.Idle) }
    var previousPostingState by remember { mutableStateOf(false) }
    var previousSuccessState by remember { mutableStateOf(false) }

    // React to state changes - CRITICAL: Check postSuccess flag
    LaunchedEffect(state.isPosting, state.postSuccess) {
        when {
            // Started posting
            state.isPosting && !previousPostingState -> {
                buttonState = PostButtonState.Loading
            }
            // Finished posting
            !state.isPosting && previousPostingState -> {
                // Check if it was actually successful
                if (state.postSuccess && !previousSuccessState) {
                    buttonState = PostButtonState.Success
                    delay(1500)
                    buttonState = PostButtonState.Idle
                } else if (!state.postSuccess) {
                    // Failed
                    buttonState = PostButtonState.Error
                    delay(2000)
                    buttonState = PostButtonState.Idle
                }
            }
        }
        previousPostingState = state.isPosting
        previousSuccessState = state.postSuccess
    }

    // Handle invalid state timeout
    LaunchedEffect(buttonState) {
        if (buttonState == PostButtonState.Invalid) {
            delay(1500)
            buttonState = PostButtonState.Idle
        }
    }

    // Get current button text based on state
    val buttonText = when (buttonState) {
        PostButtonState.Idle -> "Post tweet"
        PostButtonState.Invalid -> if (state.text.isBlank()) "Enter some text" else "Text too long"
        PostButtonState.Loading -> "Posting..."
        PostButtonState.Success -> "Posted!"
        PostButtonState.Error -> "Failed"
    }

    val scale by animateFloatAsState(
        targetValue = when (buttonState) {
            PostButtonState.Idle, PostButtonState.Invalid -> 1f
            PostButtonState.Loading -> 0.95f
            PostButtonState.Success -> 1.02f
            PostButtonState.Error -> 0.98f
        },
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    )

    val containerColor by animateColorAsState(
        targetValue = when (buttonState) {
            PostButtonState.Idle -> Color(0xFF1DA1F2)
            PostButtonState.Invalid -> Color(0xFFE63946)
            PostButtonState.Loading -> Color(0xFF1DA1F2)
            PostButtonState.Success -> Color(0xFF17BF63)
            PostButtonState.Error -> Color(0xFFE63946)
        },
        animationSpec = tween(300)
    )

    val contentColor = Color.White

    val isClickable = buttonState == PostButtonState.Idle && !state.isPosting && !isInvalid

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (isClickable) {
                    Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onPost() }
                } else {
                    Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        if (isInvalid && buttonState == PostButtonState.Idle) {
                            buttonState = PostButtonState.Invalid
                        }
                    }
                }
            ),
        color = containerColor,
        contentColor = contentColor,
        shadowElevation = 2.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when (buttonState) {
                PostButtonState.Loading -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            color = contentColor,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(20.dp)
                        )
                        TextTransition(
                            targetText = buttonText,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = contentColor,
                            config = MorphConfig(
                                duration = 350,
                                staggerDelay = 20
                            )
                        )
                    }
                }
                PostButtonState.Success -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(22.dp)
                        )
                        TextTransition(
                            targetText = buttonText,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = contentColor,
                            config = MorphConfig(
                                duration = 350,
                                staggerDelay = 20
                            )
                        )
                    }
                }
                PostButtonState.Error -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(22.dp)
                        )
                        TextTransition(
                            targetText = buttonText,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = contentColor,
                            config = MorphConfig(
                                duration = 350,
                                staggerDelay = 20
                            )
                        )
                    }
                }
                PostButtonState.Invalid, PostButtonState.Idle -> {
                    TextTransition(
                        targetText = buttonText,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = contentColor,
                        config = MorphConfig(
                            duration = 350,
                            staggerDelay = 20
                        )
                    )
                }
            }
        }
    }
}