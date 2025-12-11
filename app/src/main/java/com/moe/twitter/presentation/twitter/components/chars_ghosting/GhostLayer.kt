package com.moe.twitter.presentation.twitter.components.chars_ghosting

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.moe.twitter.ui.theme.TwitterCounterTheme
import com.moe.twitter.ui.theme.twitterColors

@Composable
fun GhostLayer(
    ghosts: List<GhostCharUi>
) {
    ghosts.forEach { ghost ->
        if (ghost.alpha <= 0f) return@forEach

        Text(
            text = ghost.char.toString(),
            modifier = Modifier.graphicsLayer(
                translationX = ghost.baseX + ghost.offsetX,
                translationY = ghost.baseY + ghost.offsetY,
                scaleX = ghost.scale,
                scaleY = ghost.scale,
                rotationZ = ghost.rotation,
                alpha = ghost.alpha
            ),
            style = TextStyle(
                fontSize = 16.sp,
                color = MaterialTheme.twitterColors.TextDark,
                lineHeight = 24.sp
            )
        )
    }
}

@Preview(name = "Ghost Characters")
@Composable
private fun GhostLayerPreview() {
    TwitterCounterTheme {
        Surface(modifier = Modifier.size(200.dp)) {
            Box {
                GhostLayer(
                    ghosts = listOf(
                        GhostCharUi(
                            id = 1L,
                            char = 'H',
                            baseX = 50f,
                            baseY = 50f,
                            offsetX = 20f,
                            offsetY = -30f,
                            alpha = 0.7f,
                            scale = 0.8f,
                            rotation = 15f
                        ),
                        GhostCharUi(
                            id = 2L,
                            char = 'i',
                            baseX = 80f,
                            baseY = 50f,
                            offsetX = 30f,
                            offsetY = -40f,
                            alpha = 0.5f,
                            scale = 0.6f,
                            rotation = -20f
                        )
                    )
                )
            }
        }
    }
}




