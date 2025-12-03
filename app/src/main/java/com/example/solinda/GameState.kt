package com.example.solinda

data class GameState(
    val stock: List<PileState>,
    val waste: List<PileState>,
    val foundations: List<PileState>,
    val tableau: List<PileState>,
    val freeCells: List<PileState>,
    var dealCount: Int = 1,
    val gameType: GameType,
    val leftMargin: Int = 20,
    val rightMargin: Int = 20,
    val tableauCardRevealFactor: Float = 0.3f
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
