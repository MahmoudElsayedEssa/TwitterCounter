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
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moe.twitter.domain.model.TweetMetrics
import com.moe.twitter.presentation.twitter.PostingState
import com.moe.twitter.presentation.twitter.TwitterState
import com.moe.twitter.presentation.twitter.components.morph.MorphConfig
import com.moe.twitter.presentation.twitter.components.morph.TextTransition
import com.moe.twitter.ui.theme.TwitterCounterTheme
import com.moe.twitter.ui.theme.twitterColors
import kotlinx.coroutines.delay

@Composable
fun PostTweetButton(
    modifier: Modifier = Modifier,
    state: TwitterState,
    onPost: () -> Unit,
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
    // Using derivedStateOf to optimize recomposition
    val buttonText by remember {
        derivedStateOf {
            when {
                showInvalidFeedback -> if (state.text.isBlank()) "Enter some text" else "Text too long"
                else -> when (state.postingState) {
                    PostingState.Idle -> "Post tweet"
                    PostingState.Posting -> "Posting..."
                    PostingState.Success -> "Posted!"
                    is PostingState.Error -> "Failed"
                }
            }
        }
    }
    val twitterColor = MaterialTheme.twitterColors
    val buttonColor by remember {
        derivedStateOf {
            when {
                showInvalidFeedback -> twitterColor.ErrorRed
                else -> when (state.postingState) {
                    PostingState.Idle -> twitterColor.TwitterBlue
                    PostingState.Posting -> twitterColor.TwitterBlue
                    PostingState.Success -> twitterColor.BrightGreen
                    is PostingState.Error -> twitterColor.ErrorRed
                }
            }
        }
    }

    val buttonIcon = when (state.postingState) {
        PostingState.Success -> Icons.Filled.CheckCircle
        is PostingState.Error -> Icons.Filled.Info
        else -> null
    }

    val showSpinner by remember {
        derivedStateOf { state.postingState == PostingState.Posting }
    }

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

    val contentColor = MaterialTheme.twitterColors.Surface

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

@Preview(name = "Post Button - Idle")
@Composable
private fun PostTweetButtonIdlePreview() {
    TwitterCounterTheme {
        Surface {
            PostTweetButton(
                state = TwitterState(
                    text = "Hello Twitter!",
                    metrics = TweetMetrics(14, 266, true),
                    postingState = PostingState.Idle
                ),
                onPost = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(name = "Post Button - Posting")
@Composable
private fun PostTweetButtonPostingPreview() {
    TwitterCounterTheme {
        Surface {
            PostTweetButton(
                state = TwitterState(
                    text = "Hello Twitter!",
                    metrics = TweetMetrics(14, 266, true),
                    postingState = PostingState.Posting
                ),
                onPost = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(name = "Post Button - Success")
@Composable
private fun PostTweetButtonSuccessPreview() {
    TwitterCounterTheme {
        Surface {
            PostTweetButton(
                state = TwitterState(
                    text = "Hello Twitter!",
                    metrics = TweetMetrics(14, 266, true),
                    postingState = PostingState.Success
                ),
                onPost = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(name = "Post Button - Error")
@Composable
private fun PostTweetButtonErrorPreview() {
    TwitterCounterTheme {
        Surface {
            PostTweetButton(
                state = TwitterState(
                    text = "Hello Twitter!",
                    metrics = TweetMetrics(14, 266, true),
                    postingState = PostingState.Error("Network error")
                ),
                onPost = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(name = "Post Button - Invalid (Empty)")
@Composable
private fun PostTweetButtonInvalidEmptyPreview() {
    TwitterCounterTheme {
        Surface {
            PostTweetButton(
                state = TwitterState(
                    text = "",
                    metrics = TweetMetrics(0, 280, true),
                    postingState = PostingState.Idle
                ),
                onPost = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(name = "Post Button - Invalid (Too Long)")
@Composable
private fun PostTweetButtonInvalidLongPreview() {
    TwitterCounterTheme {
        Surface {
            PostTweetButton(
                state = TwitterState(
                    text = "x".repeat(300),
                    metrics = TweetMetrics(300, -20, false),
                    postingState = PostingState.Idle
                ),
                onPost = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}