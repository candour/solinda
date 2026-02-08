package com.example.solinda

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.google.gson.Gson

class GameViewModel : ViewModel() {

    var gameType: GameType = GameType.KLONDIKE
    private lateinit var gameRules: GameRules

    var stock = mutableListOf<Pile>()
    var waste = mutableListOf<Pile>()
    var foundations = mutableListOf<Pile>()
    var tableau = mutableListOf<Pile>()
    var freeCells = mutableListOf<Pile>()

    var dealCount: Int = 1
    var leftMargin: Int = 20
    var rightMargin: Int = 20
    var leftMarginLandscape: Int = 20
    var rightMarginLandscape: Int = 20
    var tableauCardRevealFactor: Float = 0.3f

    init {
        initializeGameType(gameType)
    }

    fun initializeGameType(newGameType: GameType) {
        gameType = newGameType
        gameRules = when (gameType) {
            GameType.KLONDIKE -> KlondikeRules()
            GameType.FREECELL -> FreeCellRules()
            GameType.JEWELINDA -> JewelindaRules()
        }
        stock = MutableList(gameRules.stockPilesCount) { Pile(PileType.STOCK) }
        waste = MutableList(gameRules.wastePilesCount) { Pile(PileType.WASTE) }
        foundations = MutableList(gameRules.foundationPilesCount) { Pile(PileType.FOUNDATION) }
        tableau = MutableList(gameRules.tableauPilesCount) { Pile(PileType.TABLEAU) }
        freeCells = MutableList(gameRules.freeCellsCount) { Pile(PileType.FREE_CELL) }
        newGame()
    }

    fun resetGame(newGameType: GameType, newDealCount: Int) {
        dealCount = newDealCount
        if (gameType != newGameType) {
            initializeGameType(newGameType)
        } else {
            newGame()
        }
    }


    fun newGame() {
        gameRules.setupBoard(stock, waste, foundations, tableau, freeCells)
    }

    fun drawFromStock(): List<Card> {
        return gameRules.drawFromStock(stock.first(), waste.first(), dealCount)
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

        if (gameRules.canPlaceOnTableau(stack, toPile, freeCells, tableau)) {
            fromPile.removeStack(stack)
            gameRules.revealIfNeeded(fromPile)
            toPile.addStack(stack)
        }
    }

    fun moveStackToFreeCell(fromPile: Pile, stack: MutableList<Card>, toPile: Pile) {
        if (gameRules.canPlaceOnFreeCell(stack, toPile)) {
            fromPile.removeStack(stack)
            toPile.addStack(stack)
            gameRules.revealIfNeeded(fromPile)
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
        val validTableauMoves = tableau.filter { it != fromPile && gameRules.canPlaceOnTableau(listOf(card), it, freeCells, tableau) }
        val (emptyPiles, stackedPiles) = validTableauMoves.partition { it.isEmpty() }

        val targetPile = if (stackedPiles.isNotEmpty()) {
            stackedPiles.maxByOrNull { it.cards.size }
        } else if (emptyPiles.isNotEmpty()) {
            emptyPiles.first()
        } else {
            null
        }

        targetPile?.let {
            val cardToMove = fromPile.removeTopCard()!!
            gameRules.revealIfNeeded(fromPile)
            it.addCard(cardToMove)
            return it
        }

        return null
    }

    fun checkWin(): Boolean {
        return gameRules.checkWin(foundations)
    }

    fun canPlaceOnFoundation(card: Card, foundation: Pile): Boolean {
        return gameRules.canPlaceOnFoundation(card, foundation)
    }

    fun canPlaceOnTableau(stack: List<Card>, toPile: Pile): Boolean {
        return gameRules.canPlaceOnTableau(stack, toPile, freeCells, tableau)
    }

    fun findValidSubStack(pile: Pile, cardIndex: Int): MutableList<Card> {
        val originalStack = pile.cards.subList(cardIndex, pile.cards.size)

        // Check if the original stack is valid
        if (gameRules.isValidTableauStack(originalStack)) {
            return originalStack.toMutableList()
        }

        // If not, find the first valid sub-stack from the bottom up
        for (i in (cardIndex + 1) until pile.cards.size) {
            val subStack = pile.cards.subList(i, pile.cards.size)
            if (gameRules.isValidTableauStack(subStack)) {
                return subStack.toMutableList()
            }
        }

        // If no valid sub-stack is found, return the last card
        return mutableListOf(pile.cards.last())
    }

    fun isGameWinnable(): Boolean {
        return gameRules.isGameWinnable(stock, waste, tableau, freeCells)
    }

    fun autoMoveToFoundation(): Pair<Card, Pile>? {
        // First, check the free cells
        for (pile in freeCells) {
            pile.topCard()?.let { card ->
                foundations.firstOrNull { gameRules.canPlaceOnFoundation(card, it) }?.let { foundation ->
                    pile.removeTopCard()
                    foundation.addCard(card)
                    return Pair(card, foundation)
                }
            }
        }

        // Then, check the waste pile
        if (waste.isNotEmpty()) {
            waste.first().topCard()?.let { card ->
                foundations.firstOrNull { gameRules.canPlaceOnFoundation(card, it) }?.let { foundation ->
                    waste.first().removeTopCard()
                    foundation.addCard(card)
                    return Pair(card, foundation)
                }
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
            stock = stock.map { it.toPileState() },
            waste = waste.map { it.toPileState() },
            foundations = foundations.map { it.toPileState() },
            tableau = tableau.map { it.toPileState() },
            freeCells = freeCells.map { it.toPileState() },
            dealCount = dealCount,
            gameType = gameType,
            leftMargin = leftMargin,
            rightMargin = rightMargin,
            leftMarginLandscape = leftMarginLandscape,
            rightMarginLandscape = rightMarginLandscape,
            tableauCardRevealFactor = tableauCardRevealFactor
        )
        val json = Gson().toJson(gameState)
        prefs.edit().putString("game_state", json).apply()
    }

    fun loadGame(prefs: SharedPreferences) {
        val json = prefs.getString("game_state", null)
        if (json != null) {
            try {
                val gameState = Gson().fromJson(json, GameState::class.java)
                initializeGameType(gameState.gameType)
                stock = gameState.stock.map { Pile(it) }.toMutableList()
                waste = gameState.waste.map { Pile(it) }.toMutableList()
                foundations = gameState.foundations.map { Pile(it) }.toMutableList()
                tableau = gameState.tableau.map { Pile(it) }.toMutableList()
                freeCells = gameState.freeCells.map { Pile(it) }.toMutableList()
                dealCount = gameState.dealCount
                leftMargin = gameState.leftMargin
                rightMargin = gameState.rightMargin
                leftMarginLandscape = gameState.leftMarginLandscape
                rightMarginLandscape = gameState.rightMarginLandscape
                tableauCardRevealFactor = gameState.tableauCardRevealFactor
            } catch (e: Exception) {
                // Handle cases where the saved game state is invalid (e.g. old CALCULATOR game type)
                initializeGameType(GameType.KLONDIKE)
            }
        } else {
            initializeGameType(GameType.KLONDIKE)
        }
    }
}
