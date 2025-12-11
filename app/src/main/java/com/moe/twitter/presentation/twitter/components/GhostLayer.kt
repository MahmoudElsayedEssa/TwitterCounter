package com.moe.twitter.presentation.twitter.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp
import com.moe.twitter.ui.theme.TwitterCounterTheme

@Composable
fun GhostLayer(ghosts: List<GhostCharUi>) {
    val density = LocalDensity.current
    ghosts.forEach { ghost ->
        val xDp = with(density) { (ghost.baseX + ghost.offsetX).toDp() }
        val yDp = with(density) { (ghost.baseY + ghost.offsetY).toDp() }

        Text(
            text = ghost.char.toString(),
            modifier = Modifier
                .offset(x = xDp, y = yDp)
                .graphicsLayer(
                    alpha = ghost.alpha,
                    scaleX = ghost.scale,
                    scaleY = ghost.scale,
                    rotationZ = ghost.rotation
                ),
            fontSize = 16.sp,
            lineHeight = 24.sp
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




