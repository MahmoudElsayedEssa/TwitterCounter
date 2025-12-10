package com.moe.twitter.presentation.twitter

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moe.twitter.R
import com.moe.twitter.presentation.twitter.GhostEvent
import com.moe.twitter.presentation.twitter.components.DissolveTextArea
import com.moe.twitter.presentation.twitter.components.StatCard
import com.moe.twitter.presentation.twitter.components.TwitterTopBar
import kotlinx.coroutines.flow.Flow

@Composable
fun TwitterScreen(
    state: TwitterState,
    ghostEvents: kotlinx.coroutines.flow.Flow<GhostEvent>,
    onAction: (TwitterAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val onActionState by rememberUpdatedState(onAction)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
        topBar = {
            TwitterTopBar(
                title = "Twitter character count",
                onArrowClick = { /* decorative */ }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Image(
                painter = painterResource(id = R.drawable.ic_twitter_logo),
                contentDescription = "Twitter logo",
                modifier = Modifier
                    .size(56.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Characters Typed",
                    value = state.metrics.weightedLength.toString(),
                    staticSuffix = "/${state.maxChars}",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Characters Remaining",
                    value = state.metrics.remaining.toString(),
                    emphasizeNegative = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            DissolveTextArea(
                state = state,
                onTextChange = { onActionState(TwitterAction.OnTextChange(it)) },
                onTextLayout = { onActionState(TwitterAction.OnTextLayout(it)) },
                ghostEvents = ghostEvents
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onActionState(TwitterAction.OnCopy) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF27AE60),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Copy Text",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                Button(
                    onClick = { onActionState(TwitterAction.OnClear) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE63946),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Clear Text",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = { onActionState(TwitterAction.OnPost) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1DA1F2),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = state.text.isNotEmpty() &&
                        state.metrics.withinLimit &&
                        !state.isPosting
            ) {
                Text(
                    text = if (state.isPosting) "Posting..." else "Post tweet",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
