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
import com.moe.twitter.presentation.twitter.PostingState
import com.moe.twitter.presentation.twitter.TwitterState
import com.moe.twitter.presentation.twitter.components.morph.MorphConfig
import com.moe.twitter.presentation.twitter.components.morph.TextTransition
import kotlinx.coroutines.delay

@Composable
fun PostTweetButton(
    state: TwitterState,
    onPost: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Validation state
    val isInvalid = state.text.isBlank() || !state.metrics.withinLimit

    // Local UI state for showing invalid feedback
    var showInvalidFeedback by remember { mutableStateOf(false) }

    // Handle invalid click - show temporary feedback
    LaunchedEffect(showInvalidFeedback) {
        if (showInvalidFeedback) {
            delay(1500)
            showInvalidFeedback = false
        }
    }

    // Derive button appearance from robust state (single source of truth)
    val buttonText = when {
        showInvalidFeedback -> if (state.text.isBlank()) "Enter some text" else "Text too long"
        else -> when (state.postingState) {
            PostingState.Idle -> "Post tweet"
            PostingState.Posting -> "Posting..."
            PostingState.Success -> "Posted!"
            is PostingState.Error -> "Failed"
        }
    }

    val buttonColor = when {
        showInvalidFeedback -> Color(0xFFE63946) // Red for invalid
        else -> when (state.postingState) {
            PostingState.Idle -> Color(0xFF1DA1F2) // Twitter blue
            PostingState.Posting -> Color(0xFF1DA1F2) // Twitter blue
            PostingState.Success -> Color(0xFF17BF63) // Green
            is PostingState.Error -> Color(0xFFE63946) // Red
        }
    }

    val buttonIcon = when (state.postingState) {
        PostingState.Success -> Icons.Filled.CheckCircle
        is PostingState.Error -> Icons.Filled.Info
        else -> null
    }

    val showSpinner = state.postingState == PostingState.Posting

    // Smooth animated scale based on state
    val scale by animateFloatAsState(
        targetValue = when {
            showInvalidFeedback -> 0.98f
            else -> when (state.postingState) {
                PostingState.Idle -> 1f
                PostingState.Posting -> 0.95f
                PostingState.Success -> 1.02f
                is PostingState.Error -> 0.98f
            }
        },
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "button_scale"
    )

    // Smooth animated color transition
    val containerColor by animateColorAsState(
        targetValue = buttonColor,
        animationSpec = tween(300),
        label = "button_color"
    )

    val contentColor = Color.White

    // Button is clickable only when idle and valid
    val isClickable = state.postingState == PostingState.Idle && !isInvalid

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                if (isClickable) {
                    onPost()
                } else if (isInvalid && state.postingState == PostingState.Idle) {
                    // Show invalid feedback
                    showInvalidFeedback = true
                }
            },
        color = containerColor,
        contentColor = contentColor,
        shadowElevation = 2.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Show spinner when posting
                if (showSpinner) {
                    CircularProgressIndicator(
                        color = contentColor,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Show icon for success/error states
                buttonIcon?.let { icon ->
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Elegant morphing text
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