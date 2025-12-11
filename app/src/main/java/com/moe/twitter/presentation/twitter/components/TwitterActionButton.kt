package com.moe.twitter.presentation.twitter.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moe.twitter.ui.theme.TwitterCounterTheme
import com.moe.twitter.ui.theme.twitterColors

@Composable
fun TwitterActionButton(
    modifier: Modifier = Modifier,
    text: String,
    background: Color,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(40.dp),                      // compact height
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = background,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

@Preview(name = "Copy Button")
@Composable
private fun TwitterActionButtonCopyPreview() {
    TwitterCounterTheme {
        Surface {
            TwitterActionButton(
                text = "Copy Text",
                background = MaterialTheme.twitterColors.SuccessGreen,
                onClick = {}
            )
        }
    }
}

@Preview(name = "Clear Button")
@Composable
private fun TwitterActionButtonClearPreview() {
    TwitterCounterTheme {
        Surface {
            TwitterActionButton(
                text = "Clear Text",
                background = MaterialTheme.twitterColors.ErrorRed,
                onClick = {}
            )
        }
    }
}
