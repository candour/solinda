package com.example.solinda

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class Suit { HEARTS, DIAMONDS, CLUBS, SPADES }

class Card(
    val suit: Suit,
    val rank: Int // 1 = Ace ... 13 = King
) {
    var faceUp by mutableStateOf(false)
    var x by mutableStateOf(0f)
    var y by mutableStateOf(0f)

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

    constructor(suit: Suit, rank: Int, faceUp: Boolean, x: Float = 0f, y: Float = 0f) : this(suit, rank) {
        this.faceUp = faceUp
        this.x = x
        this.y = y
    }

    constructor(cardState: CardState) : this(
        cardState.suit,
        cardState.rank,
        cardState.faceUp,
        cardState.x,
        cardState.y
    )

    fun toCardState() = CardState(suit, rank, faceUp, x, y)

    fun copy(faceUp: Boolean = this.faceUp, x: Float = this.x, y: Float = this.y): Card {
        return Card(suit, rank, faceUp, x, y)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Card) return false
        return suit == other.suit && rank == other.rank
    }

    override fun hashCode(): Int {
        var result = suit.hashCode()
        result = 31 * result + rank
        return result
    }
}
