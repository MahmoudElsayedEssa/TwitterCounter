package com.moe.twitter.presentation.twitter.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
import com.moe.twitter.presentation.twitter.components.GhostLayer

@Composable
fun DissolveTextArea(
    state: TwitterState,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val overflowAndErrorsTransform = remember(state.text, state.errors, state.maxChars) {
        VisualTransformation { original: AnnotatedString ->
            val styled = AnnotatedString.Builder().apply {
                append(original)

                if (original.text.length > state.maxChars) {
                    addStyle(
                        style = SpanStyle(
                            background = Color(0x33E0245E),
                            color = Color(0xFFE0245E)
                        ),
                        start = state.maxChars,
                        end = original.length
                    )
                }

                state.errors.forEach { err ->
                    val safeStart = err.start.coerceIn(0, original.text.length)
                    val safeEnd = err.end.coerceIn(safeStart, original.text.length)
                    if (safeStart >= safeEnd) return@forEach

                    val color = when (err.issueType) {
                        "grammar", "typographical" -> Color(0xFFE0245E)
                        "style" -> Color(0xFFE0A400)
                        else -> Color(0xFFE0245E)
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(Color(0xFFF7F9FA), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE1E8ED), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        BasicTextField(
            value = state.text,
            onValueChange = onTextChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(188.dp),
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = Color(0xFF14171A),
                lineHeight = 24.sp
            ),
            cursorBrush = SolidColor(Color(0xFF1DA1F2)),
            visualTransformation = overflowAndErrorsTransform
        )

        if (state.text.isEmpty()) {
            Text(
                "What's happening?",
                fontSize = 16.sp,
                color = Color(0xFFAAB8C2),
                lineHeight = 24.sp
            )
        }

        GhostLayer(ghosts = state.ghosts)
    }
}


