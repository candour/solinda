package com.example.solinda

data class GameState(
    val stock: PileState,
    val waste: PileState,
    val foundations: List<PileState>,
    val tableau: List<PileState>
)

data class PileState(
    val cards: List<CardState>,
    val type: PileType
)

data class CardState(
    val suit: Suit,
    val rank: Int,
    var faceUp: Boolean = false,
    var x: Float = 0f,
    var y: Float = 0f
)
