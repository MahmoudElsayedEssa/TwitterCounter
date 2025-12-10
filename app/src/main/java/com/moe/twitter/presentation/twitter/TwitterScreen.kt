package com.moe.twitter.presentation.twitter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.moe.twitter.presentation.twitter.components.DissolveTextArea
import com.moe.twitter.presentation.twitter.components.PostTweetButton
import com.moe.twitter.presentation.twitter.components.StatCard
import com.moe.twitter.presentation.twitter.components.TwitterActionButton
import com.moe.twitter.presentation.twitter.components.TwitterLogo
import com.moe.twitter.presentation.twitter.components.TwitterTopBar
import kotlinx.coroutines.flow.Flow

@Composable
fun TwitterScreen(
    state: TwitterState,
    ghostEvents: Flow<GhostEvent>,
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
                .imePadding()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            TwitterLogo(
                postingState = state.postingState,
                modifier = Modifier
                    .size(64.dp)
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
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
                    .weight(1f, fill = false) // can shrink when keyboard appears
                ,
                state = state,
                onTextChange = { onActionState(TwitterAction.OnTextChange(it)) },
                onTextLayout = { onActionState(TwitterAction.OnTextLayout(it)) },
                ghostEvents = ghostEvents
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TwitterActionButton(
                    text = "Copy Text",
                    background = Color(0xFF27AE60),
                    onClick = { onActionState(TwitterAction.OnCopy) },
                )


                TwitterActionButton(
                    text = "Clear Text",
                    background = Color(0xFFE63946),
                    onClick = { onActionState(TwitterAction.OnClear) },
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            PostTweetButton(
                state = state,
                onPost = { onActionState(TwitterAction.OnPost) },
                modifier = Modifier
            )
        }
    }
}