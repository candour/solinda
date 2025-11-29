package com.example.solinda

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.google.gson.Gson

class GameViewModel : ViewModel() {

    private val gameRules: GameRules = KlondikeRules()

    var stock = Pile(PileType.STOCK)
    var waste = Pile(PileType.WASTE)
    var foundations = List(gameRules.foundationPilesCount) { Pile(PileType.FOUNDATION) }
    var tableau = List(gameRules.tableauPilesCount) { Pile(PileType.TABLEAU) }

    var dealCount: Int = 1

    fun newGame() {
        gameRules.setupBoard(stock, waste, foundations, tableau)
    }

    fun drawFromStock(): List<Card> {
        return gameRules.drawFromStock(stock, waste, dealCount)
    }

    fun moveToFoundation(fromPile: Pile, foundation: Pile) {
        val card = fromPile.topCard() ?: return
        if (gameRules.canPlaceOnFoundation(card, foundation)) {
            val cardToMove = fromPile.removeTopCard()
            if (cardToMove != null) {
                foundation.addCard(cardToMove)
                gameRules.revealIfNeeded(fromPile)
            }
        }
    }

    fun moveStackToTableau(fromPile: Pile, stack: MutableList<Card>, toPile: Pile) {
        if (fromPile.type == PileType.FOUNDATION && stack.size > 1) return

        if (gameRules.canPlaceOnTableau(stack, toPile)) {
            fromPile.removeStack(stack)
            gameRules.revealIfNeeded(fromPile)
            toPile.addStack(stack)
        }
    }

    fun autoMoveCard(card: Card, fromPile: Pile): Pile? {
        if (card != fromPile.topCard() || fromPile.type == PileType.FOUNDATION) {
            return null
        }

        // Priority 1: Move to a foundation
        foundations.firstOrNull { gameRules.canPlaceOnFoundation(card, it) }?.let { targetFoundation ->
            val cardToMove = fromPile.removeTopCard()!!
            gameRules.revealIfNeeded(fromPile)
            targetFoundation.addCard(cardToMove)
            return targetFoundation
        }

        // Priority 2: Move to a tableau pile
        tableau.firstOrNull { it != fromPile && gameRules.canPlaceOnTableau(listOf(card), it) }?.let { targetTableau ->
            val cardToMove = fromPile.removeTopCard()!!
            gameRules.revealIfNeeded(fromPile)
            targetTableau.addCard(cardToMove)
            return targetTableau
        }

        return null
    }

    fun checkWin(): Boolean {
        return gameRules.checkWin(foundations)
    }

    fun autoMoveToFoundation(): Pair<Card, Pile>? {
        // First, check the waste pile
        waste.topCard()?.let { card ->
            foundations.firstOrNull { gameRules.canPlaceOnFoundation(card, it) }?.let { foundation ->
                waste.removeTopCard()
                foundation.addCard(card)
                return Pair(card, foundation)
            }
        }

        // Then, check the tableau piles
        for (pile in tableau) {
            pile.topCard()?.let { card ->
                foundations.firstOrNull { gameRules.canPlaceOnFoundation(card, it) }?.let { foundation ->
                    pile.removeTopCard()
                    foundation.addCard(card)
                    return Pair(card, foundation)
                }
            }
        }

        return null
    }

    fun saveGame(prefs: SharedPreferences) {
        val gameState = GameState(
            stock = stock.toPileState(),
            waste = waste.toPileState(),
            foundations = foundations.map { it.toPileState() },
            tableau = tableau.map { it.toPileState() },
            dealCount = dealCount
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
            dealCount = gameState.dealCount
        } else {
            newGame()
        }
    }
}
