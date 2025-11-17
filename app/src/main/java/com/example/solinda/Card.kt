package com.example.solinda

enum class Suit { HEARTS, DIAMONDS, CLUBS, SPADES }

data class Card(
    val suit: Suit,
    val rank: Int, // 1 = Ace ... 13 = King
    var faceUp: Boolean = false,
    var x: Float = 0f,
    var y: Float = 0f
) {
    val color: String
        get() = if (suit == Suit.HEARTS || suit == Suit.DIAMONDS) "RED" else "BLACK"

    val imageName: String
        get() {
            val suitChar = when (suit) {
                Suit.HEARTS -> "h"
                Suit.DIAMONDS -> "d"
                Suit.CLUBS -> "c"
                Suit.SPADES -> "s"
            }
            return suitChar + rank
        }

    override fun toString(): String {
        val ranks = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
        return "${ranks[rank - 1]} of $suit"
    }

    constructor(cardState: CardState) : this(cardState.suit, cardState.rank, cardState.faceUp)

    fun toCardState() = CardState(suit, rank, faceUp)
}
