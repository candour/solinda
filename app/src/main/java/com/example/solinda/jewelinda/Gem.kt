package com.example.solinda.jewelinda

import androidx.compose.ui.graphics.Color
import java.util.UUID

enum class GemType {
    RED, BLUE, GREEN, YELLOW, PURPLE, ORANGE
}

data class Gem(
    val id: UUID = UUID.randomUUID(),
    val type: GemType,
    val posX: Int,
    val posY: Int,
    val isBomb: Boolean = false
)

fun getGemColor(type: GemType): Color {
    return when (type) {
        GemType.RED -> Color.Red
        GemType.BLUE -> Color.Blue
        GemType.GREEN -> Color.Green
        GemType.YELLOW -> Color.Yellow
        GemType.PURPLE -> Color.Magenta
        GemType.ORANGE -> Color(0xFFFFA500)
    }
}
