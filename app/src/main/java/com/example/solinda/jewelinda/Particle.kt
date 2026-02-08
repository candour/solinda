package com.example.solinda.jewelinda

import androidx.compose.ui.graphics.Color
import java.util.UUID

data class Particle(
    val id: UUID = UUID.randomUUID(),
    val x: Float,
    val y: Float,
    val velocityX: Float,
    val velocityY: Float,
    val alpha: Float,
    val scale: Float,
    val color: Color
)
