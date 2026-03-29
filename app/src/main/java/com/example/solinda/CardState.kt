package com.example.solinda

data class CardState(
    val suit: Suit,
    val rank: Int,
    var faceUp: Boolean = false,
    var x: Float = 0f,
    var y: Float = 0f
)
