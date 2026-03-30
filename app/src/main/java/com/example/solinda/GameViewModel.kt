package com.example.solinda

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class GameViewModel : ViewModel() {

    var gameType: GameType by mutableStateOf(GameType.KLONDIKE)
    private lateinit var gameRules: CardGameRules

    var stock = mutableStateListOf<Pile>()
    var waste = mutableStateListOf<Pile>()
    var foundations = mutableStateListOf<Pile>()
    var tableau = mutableStateListOf<Pile>()
    var freeCells = mutableStateListOf<Pile>()

    var dealCount: Int by mutableIntStateOf(Constants.DEFAULT_DEAL_COUNT)
    var leftMargin: Int by mutableIntStateOf(Constants.DEFAULT_MARGIN)
    var rightMargin: Int by mutableIntStateOf(Constants.DEFAULT_MARGIN)
    var leftMarginLandscape: Int by mutableIntStateOf(Constants.DEFAULT_MARGIN_LANDSCAPE_LEFT)
    var rightMarginLandscape: Int by mutableIntStateOf(Constants.DEFAULT_MARGIN_LANDSCAPE_RIGHT)
    var tableauCardRevealFactor: Float by mutableFloatStateOf(Constants.DEFAULT_TABLEAU_REVEAL_FACTOR)
    var isHapticsEnabled: Boolean by mutableStateOf(true)

    init {
        initializeGameType(gameType)
    }

    fun initializeGameType(newGameType: GameType) {
        gameType = newGameType
        val rules = when (gameType) {
            GameType.KLONDIKE -> KlondikeRules()
            GameType.FREECELL -> FreeCellRules()
            else -> KlondikeRules() // Fallback for card games
        }
        gameRules = rules
        stock.clear()
        stock.addAll(List(gameRules.stockPilesCount) { Pile(PileType.STOCK) })
        waste.clear()
        waste.addAll(List(gameRules.wastePilesCount) { Pile(PileType.WASTE) })
        foundations.clear()
        foundations.addAll(List(gameRules.foundationPilesCount) { Pile(PileType.FOUNDATION) })
        tableau.clear()
        tableau.addAll(List(gameRules.tableauPilesCount) { Pile(PileType.TABLEAU) })
        freeCells.clear()
        freeCells.addAll(List(gameRules.freeCellsCount) { Pile(PileType.FREE_CELL) })
        newGame()
    }

    fun resetGame(newGameType: GameType, newDealCount: Int) {
        dealCount = newDealCount
        initializeGameType(newGameType)
    }


    fun newGame() {
        gameRules.setupBoard(stock, waste, foundations, tableau, freeCells)
    }

    fun drawFromStock(): List<Card> {
        if (stock.isEmpty() || waste.isEmpty()) return emptyList()
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

    fun autoMoveCard(card: Card, fromPile: Pile, skipModelUpdate: Boolean = false): Pile? {
        if (card != fromPile.topCard() || fromPile.type == PileType.FOUNDATION) {
            return null
        }

        // Priority 1: Move to a foundation
        foundations.firstOrNull { gameRules.canPlaceOnFoundation(card, it) }?.let { targetFoundation ->
            if (!skipModelUpdate) {
                val cardToMove = fromPile.removeTopCard()!!
                gameRules.revealIfNeeded(fromPile)
                targetFoundation.addCard(cardToMove)
            }
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
            if (!skipModelUpdate) {
                val cardToMove = fromPile.removeTopCard()!!
                gameRules.revealIfNeeded(fromPile)
                it.addCard(cardToMove)
            }
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

    fun canPlaceOnFreeCell(stack: List<Card>, freeCell: Pile): Boolean {
        return gameRules.canPlaceOnFreeCell(stack, freeCell)
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

    var onAutoMovePerformed: ((Card, Pile, Pile) -> Unit)? = null

    fun checkForAutoComplete() {
        if (gameType == GameType.FREECELL || isGameWinnable()) {
            val moved = autoMoveToFoundation()
            if (moved != null) {
                // If a callback is registered, we let the UI handle the move (and possibly animate it)
                // However, autoMoveToFoundation already performed the move in the model.
                // We might need to rethink this to support animations for cascading moves.
            }
        }
    }

    fun autoMoveToFoundation(skipModelUpdate: Boolean = false): Triple<Card, Pile, Pile>? {
        // First, check the free cells
        for (pile in freeCells) {
            val card = pile.topCard() ?: continue
            foundations.firstOrNull { gameRules.canPlaceOnFoundation(card, it) }?.let { foundation ->
                if (!skipModelUpdate) {
                    pile.removeTopCard()
                    foundation.addCard(card)
                    gameRules.revealIfNeeded(pile)
                }
                return Triple(card, pile, foundation)
            }
        }

        // Then, check the waste pile
        waste.firstOrNull()?.let { wastePile ->
            val card = wastePile.topCard() ?: return@let
            foundations.firstOrNull { gameRules.canPlaceOnFoundation(card, it) }?.let { foundation ->
                if (!skipModelUpdate) {
                    wastePile.removeTopCard()
                    foundation.addCard(card)
                    gameRules.revealIfNeeded(wastePile)
                }
                return Triple(card, wastePile, foundation)
            }
        }


        // Then, check the tableau piles
        for (pile in tableau) {
            val card = pile.topCard() ?: continue
            foundations.firstOrNull { gameRules.canPlaceOnFoundation(card, it) }?.let { foundation ->
                if (!skipModelUpdate) {
                    pile.removeTopCard()
                    foundation.addCard(card)
                    gameRules.revealIfNeeded(pile)
                }
                return Triple(card, pile, foundation)
            }
        }

        return null
    }

    fun saveGame(repository: GameRepository) {
        val existingGameState = repository.loadGame()

        val solitaireData = if (gameType == GameType.KLONDIKE || gameType == GameType.FREECELL) {
            SolitaireData(
                stock = stock.map { it.toPileState() },
                waste = waste.map { it.toPileState() },
                foundations = foundations.map { it.toPileState() },
                tableau = tableau.map { it.toPileState() },
                freeCells = freeCells.map { it.toPileState() }
            )
        } else {
            existingGameState?.solitaireData
        }

        val commonSettings = CommonSettings(
            gameType = gameType,
            dealCount = dealCount,
            leftMargin = leftMargin,
            rightMargin = rightMargin,
            leftMarginLandscape = leftMarginLandscape,
            rightMarginLandscape = rightMarginLandscape,
            tableauCardRevealFactor = tableauCardRevealFactor,
            isHapticsEnabled = isHapticsEnabled
        )

        val updatedGameState = GameState(
            commonSettings = commonSettings,
            solitaireData = solitaireData,
            jewelindaData = existingGameState?.jewelindaData
        )

        repository.saveGame(updatedGameState)
    }

    fun loadGame(repository: GameRepository) {
        val gameState = repository.loadGame()
        if (gameState != null) {
            val settings = gameState.commonSettings

            // First, initialize the game type to set up the rules and pile counts
            initializeGameType(settings.gameType)

            // Only load saved card data if it matches the current game type's structure
            gameState.solitaireData?.let { data: SolitaireData ->
                if (data.stock.size == gameRules.stockPilesCount &&
                    data.waste.size == gameRules.wastePilesCount &&
                    data.foundations.size == gameRules.foundationPilesCount &&
                    data.tableau.size == gameRules.tableauPilesCount &&
                    data.freeCells.size == gameRules.freeCellsCount) {

                    stock.clear()
                    stock.addAll(data.stock.map { Pile(it) })
                    waste.clear()
                    waste.addAll(data.waste.map { Pile(it) })
                    foundations.clear()
                    foundations.addAll(data.foundations.map { Pile(it) })
                    tableau.clear()
                    tableau.addAll(data.tableau.map { Pile(it) })
                    freeCells.clear()
                    freeCells.addAll(data.freeCells.map { Pile(it) })
                }
            }

            dealCount = settings.dealCount
            leftMargin = settings.leftMargin
            rightMargin = settings.rightMargin
            leftMarginLandscape = settings.leftMarginLandscape
            rightMarginLandscape = settings.rightMarginLandscape
            tableauCardRevealFactor = settings.tableauCardRevealFactor
            isHapticsEnabled = settings.isHapticsEnabled
        } else {
            initializeGameType(GameType.KLONDIKE)
        }
    }
}
