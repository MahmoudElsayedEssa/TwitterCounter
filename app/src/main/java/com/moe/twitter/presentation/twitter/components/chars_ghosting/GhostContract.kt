package com.moe.twitter.presentation.twitter.components.chars_ghosting

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D

data class GhostSeed(
    val id: Long,
    val char: Char,
    val baseX: Float,
    val baseY: Float,
    val order: Int = 0
)

data class GhostCharUi(
    val id: Long,
    val char: Char,
    val baseX: Float,
    val baseY: Float,
    val offsetX: Float,
    val offsetY: Float,
    val alpha: Float,
    val scale: Float,
    val rotation: Float
)

data class GhostRender(
    val id: Long,
    val char: Char,
    val baseX: Float,
    val baseY: Float,
    val offsetX: Animatable<Float, AnimationVector1D>,
    val offsetY: Animatable<Float, AnimationVector1D>,
    val alpha: Animatable<Float, AnimationVector1D>,
    val scale: Animatable<Float, AnimationVector1D>,
    val rotation: Animatable<Float, AnimationVector1D>
) {
    fun toUi(): GhostCharUi = GhostCharUi(
        id = id,
        char = char,
        baseX = baseX,
        baseY = baseY,
        offsetX = offsetX.value,
        offsetY = offsetY.value,
        alpha = alpha.value,
        scale = scale.value,
        rotation = rotation.value
    )

    companion object {
        fun fromSeed(seed: GhostSeed): GhostRender = GhostRender(
            id = seed.id,
            char = seed.char,
            baseX = seed.baseX,
            baseY = seed.baseY,
            offsetX = Animatable(0f),
            offsetY = Animatable(0f),
            alpha = Animatable(1f),
            scale = Animatable(1f),
            rotation = Animatable(0f)
        )
    }
}
