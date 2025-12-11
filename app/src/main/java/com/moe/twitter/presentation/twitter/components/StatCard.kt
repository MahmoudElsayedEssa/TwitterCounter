package com.moe.twitter.presentation.twitter.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moe.twitter.presentation.twitter.components.AnimatedNumber
import com.moe.twitter.ui.theme.TwitterCounterTheme
import com.moe.twitter.ui.theme.twitterColors

@Composable
fun StatCard(
    title: String,
    value: String,
    staticSuffix: String? = null,
    emphasizeNegative: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.border(
            width = 2.dp,
            color = MaterialTheme.twitterColors.SurfaceBlue,
            shape = RoundedCornerShape(12.dp)
        ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.twitterColors.Surface),
        border = CardDefaults.outlinedCardBorder(enabled = true),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.twitterColors.SurfaceBlue)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.twitterColors.TextPrimary
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 46.dp)
                    .background(MaterialTheme.twitterColors.Surface)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                val isNegative = emphasizeNegative && value.startsWith("-")

                val numberColor = if (isNegative) {
                    MaterialTheme.twitterColors.TwitterRed
                } else {
                    MaterialTheme.twitterColors.TextPrimary
                }

                val numberStyle = remember(numberColor) {
                    TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = numberColor
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    AnimatedNumber(
                        value = value,
                        style = numberStyle
                    )
                    staticSuffix?.let {
                        Text(
                            text = it,
                            style = numberStyle
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Stat Card - Normal")
@Composable
private fun StatCardNormalPreview() {
    TwitterCounterTheme {
        Surface {
            StatCard(
                title = "Characters Typed",
                value = "245",
                staticSuffix = "/280",
                modifier = Modifier.width(160.dp)
            )
        }
    }
}

@Preview(name = "Stat Card - Negative")
@Composable
private fun StatCardNegativePreview() {
    TwitterCounterTheme {
        Surface {
            StatCard(
                title = "Characters Remaining",
                value = "-15",
                emphasizeNegative = true,
                modifier = Modifier.width(160.dp)
            )
        }
    }
}

@Preview(name = "Stat Card - Zero")
@Composable
private fun StatCardZeroPreview() {
    TwitterCounterTheme {
        Surface {
            StatCard(
                title = "Characters Remaining",
                value = "0",
                modifier = Modifier.width(160.dp)
            )
        }
    }
}


