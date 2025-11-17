package com.example.solinda

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.google.gson.Gson

class GameViewModel : ViewModel() {

    var stock = Pile(PileType.STOCK)
    var waste = Pile(PileType.WASTE)
    var foundations = List(4) { Pile(PileType.FOUNDATION) }
    var tableau = List(7) { Pile(PileType.TABLEAU) }

    fun newGame() {
        // Clear all piles
        stock.cards.clear()
        waste.cards.clear()
        foundations.forEach { it.cards.clear() }
        tableau.forEach { it.cards.clear() }

        val deck = mutableListOf<Card>()
        for (suit in Suit.entries) {
            for (rank in 1..13) deck.add(Card(suit, rank))
        }
        deck.shuffle()

        // Deal tableau
        tableau.forEachIndexed { index, pile ->
            for (i in 0..index) {
                val card = deck.removeFirst()
                card.faceUp = (i == index)
                pile.addCard(card)
            }
        }

        stock.cards.addAll(deck)
    }

    fun drawFromStock(): Card? {
        if (stock.cards.isEmpty()) {
            // Recycle waste into stock
            stock.cards.addAll(waste.cards.map { it.copy(faceUp = false) }.asReversed())
            waste.cards.clear()
            return null
        } else {
            val card = stock.removeTopCard()!!
            card.faceUp = true
            waste.addCard(card)
            return card
        }
    }

    fun canPlaceOnFoundation(card: Card, foundation: Pile): Boolean {
        val top = foundation.topCard()
        return when {
            foundation.isEmpty() -> card.rank == 1
            top != null && top.suit == card.suit && card.rank == top.rank + 1 -> true
            else -> false
        }
    }

    fun canPlaceOnTableau(stack: List<Card>, tableauPile: Pile): Boolean {
        if (stack.isEmpty()) return false
        val top = tableauPile.topCard()
        val bottomCard = stack.first()
        return when {
            tableauPile.isEmpty() -> bottomCard.rank == 13
            top != null && top.color != bottomCard.color && bottomCard.rank == top.rank - 1 -> true
            else -> false
        }
    }

    fun moveToFoundation(fromPile: Pile, foundation: Pile) {
        val card = fromPile.topCard() ?: return
        if (canPlaceOnFoundation(card, foundation)) {
            val cardToMove = fromPile.removeTopCard()
            if (cardToMove != null) {
                foundation.addCard(cardToMove)
                revealIfNeeded(fromPile)
            }
        }
    }

    fun moveStackToTableau(fromPile: Pile, stack: MutableList<Card>, toPile: Pile) {
        if (fromPile.type == PileType.FOUNDATION && stack.size > 1) return

        if (canPlaceOnTableau(stack, toPile)) {
            fromPile.removeStack(stack)
            revealIfNeeded(fromPile)
            toPile.addStack(stack)
        }
    }

    fun autoMoveCard(card: Card, fromPile: Pile): Pile? {
        if (card != fromPile.topCard() || fromPile.type == PileType.FOUNDATION) {
            return null
        }

        // Priority 1: Move to a foundation
        foundations.firstOrNull { canPlaceOnFoundation(card, it) }?.let { targetFoundation ->
            val cardToMove = fromPile.removeTopCard()!!
            revealIfNeeded(fromPile)
            targetFoundation.addCard(cardToMove)
            return targetFoundation
        }

        // Priority 2: Move to a tableau pile
        tableau.firstOrNull { it != fromPile && canPlaceOnTableau(listOf(card), it) }?.let { targetTableau ->
            val cardToMove = fromPile.removeTopCard()!!
            revealIfNeeded(fromPile)
            targetTableau.addCard(cardToMove)
            return targetTableau
        }

        return null
    }

    private fun revealIfNeeded(pile: Pile) {
        if (pile.type == PileType.TABLEAU && pile.topCard()?.faceUp == false) {
            pile.topCard()?.faceUp = true
        }
    }

    fun checkWin(): Boolean {
        return foundations.all { it.cards.size == 13 }
    }

    fun saveGame(prefs: SharedPreferences) {
        val gameState = GameState(
            stock = stock.toPileState(),
            waste = waste.toPileState(),
            foundations = foundations.map { it.toPileState() },
            tableau = tableau.map { it.toPileState() }
        )
        val json = Gson().toJson(gameState)
        prefs.edit().putString("game_state", json).apply()
    }

    fun loadGame(prefs: SharedPreferences) {
        val json = prefs.getString("game_state", null)
        if (json != null) {
            val gameState = Gson().fromJson(json, GameState::class.java)
            stock = Pile(gameState.stock)
            waste = Pile(gameState.waste)
            foundations = gameState.foundations.map { Pile(it) }
            tableau = gameState.tableau.map { Pile(it) }
        } else {
            newGame()
        }
    }
}
