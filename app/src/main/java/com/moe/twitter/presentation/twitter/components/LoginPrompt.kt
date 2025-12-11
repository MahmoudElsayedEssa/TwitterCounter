package com.moe.twitter.presentation.twitter.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.moe.twitter.ui.theme.TwitterCounterTheme
import com.moe.twitter.ui.theme.twitterColors

@Composable
fun LoginPrompt(
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TwitterLogo(
            postingState = com.moe.twitter.presentation.twitter.PostingState.Idle,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Login Required",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.twitterColors.TextPrimary
        )

        Text(
            text = "You need to login to post tweets",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.twitterColors.TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onLoginClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.twitterColors.TwitterBlue
            )
        ) {
            Text(
                text = "Login with Twitter",
                color = Color.White
            )
        }
    }
}

@Preview(name = "Login Prompt", showBackground = true)
@Composable
private fun LoginPromptPreview() {
    TwitterCounterTheme {
        LoginPrompt(
            onLoginClick = {}
        )
    }
}
