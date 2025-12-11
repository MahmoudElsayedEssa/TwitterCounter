package com.moe.twitter.presentation.twitter.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.moe.twitter.ui.theme.TwitterCounterTheme
import com.moe.twitter.ui.theme.twitterColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwitterTopBar(
    title: String,
    onArrowClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.twitterColors.TextPrimary
                )
            )
        },
        actions = {
            IconButton(onClick = onArrowClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.twitterColors.IconGray
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.twitterColors.Surface,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Preview(name = "Top Bar")
@Composable
private fun TwitterTopBarPreview() {
    TwitterCounterTheme {
        TwitterTopBar(
            title = "Twitter character count",
            onArrowClick = {}
        )
    }
}


