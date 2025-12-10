package com.moe.twitter.presentation.twitter.components

import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp
import com.moe.twitter.presentation.twitter.GhostCharUi

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



