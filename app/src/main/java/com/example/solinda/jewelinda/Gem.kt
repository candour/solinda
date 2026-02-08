package com.example.solinda.jewelinda

import java.util.UUID

enum class GemType {
    RED, BLUE, GREEN, YELLOW, PURPLE, ORANGE
}

data class Gem(
    val id: UUID = UUID.randomUUID(),
    val type: GemType,
    val posX: Int,
    val posY: Int
)
