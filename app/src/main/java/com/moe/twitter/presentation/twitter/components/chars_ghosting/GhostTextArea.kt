package com.moe.twitter.presentation.twitter.components.chars_ghosting

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moe.twitter.presentation.twitter.TwitterState
import com.moe.twitter.ui.theme.twitterColors

@Composable
fun GhostTextArea(
    modifier: Modifier = Modifier,
    state: TwitterState,
    onTextChange: (String) -> Unit,
) {
    val twitterColor = MaterialTheme.twitterColors
    val maxChars = 280

    val ghostController = rememberGhostController()

    // Track previous text for external changes (e.g. clear button)
    val previousText = remember { mutableStateOf("") }

    // Visual transformation for overflow + language errors
    val overflowAndErrorsTransform = remember(state.errors, maxChars) {
        VisualTransformation { original: AnnotatedString ->
            val styled = AnnotatedString.Builder().apply {
                append(original)

                // Overflow highlighting
                if (original.text.length > maxChars) {
                    addStyle(
                        style = SpanStyle(
                            background = twitterColor.ErrorBackground,
                            color = twitterColor.TwitterRed
                        ),
                        start = maxChars,
                        end = original.length
                    )
                }

                // Error underlines
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
                            color = color,
                            textDecoration = TextDecoration.Underline
                        ),
                        start = safeStart,
                        end = safeEnd
                    )
                }
            }.toAnnotatedString()

            TransformedText(styled, OffsetMapping.Identity)
        }
    }

    // Handle text changes coming from the text field (user input)
    val handleTextChangeWithGhost: (String) -> Unit = { newText ->
        val oldText = previousText.value

        // Let the ghost engine handle backspace ghosts
        ghostController.onIncomingTextChange(
            oldText = oldText,
            newText = newText
        )

        previousText.value = newText
        onTextChange(newText)
    }

    // Handle external text changes (e.g. clear button, reset draft)
    LaunchedEffect(state.text) {
        if (state.text != previousText.value) {
            if (state.text.isEmpty() && previousText.value.isNotEmpty()) {
                // Trigger explosion ghosts for full clear
                ghostController.onExternalClear(previousText.value)
            }
            previousText.value = state.text
        }
    }

    // rgba(6, 26, 64, 0.04)
    val shadowColor = Color(red = 6, green = 26, blue = 64, alpha = (0.4f * 255).toInt())

    Box(
        modifier = modifier
            .background(MaterialTheme.twitterColors.Surface, RoundedCornerShape(12.dp))
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(12.dp),
                clip = false,
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .background(
                MaterialTheme.twitterColors.Surface,
                RoundedCornerShape(12.dp)
            )
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
                fontSize = 16.sp,
                color = MaterialTheme.twitterColors.TextDark,
                lineHeight = 24.sp
            ),
            cursorBrush = SolidColor(MaterialTheme.twitterColors.TwitterBlue),
            visualTransformation = overflowAndErrorsTransform,
            onTextLayout = { newLayout ->
                ghostController.onLayout(newLayout)
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

        GhostLayer(ghosts = ghostController.ghostsUi)
    }
}