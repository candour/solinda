package com.example.solinda

interface GameRules {
    fun setupBoard()
    fun checkWin(): Boolean = false
}

interface CardGameRules : GameRules {
    val foundationPilesCount: Int
    val tableauPilesCount: Int
    val freeCellsCount: Int
    val stockPilesCount: Int
    val wastePilesCount: Int

    fun setupBoard(
        stock: List<Pile>,
        waste: List<Pile>,
        foundations: List<Pile>,
        tableau: List<Pile>,
        freeCells: List<Pile>
    )

    override fun setupBoard() {
        // Default implementation for the generic setupBoard if called
    }

    fun drawFromStock(stock: Pile, waste: Pile, dealCount: Int): List<Card>
    fun canPlaceOnFoundation(card: Card, foundation: Pile): Boolean
    fun canPlaceOnTableau(stack: List<Card>, toPile: Pile, freeCells: List<Pile>, tableau: List<Pile>): Boolean
    fun canPlaceOnFreeCell(stack: List<Card>, freeCell: Pile): Boolean
    fun isValidTableauStack(stack: List<Card>): Boolean
    fun revealIfNeeded(pile: Pile)
    fun checkWin(foundations: List<Pile>): Boolean
    override fun checkWin(): Boolean = false // Default implementation

    fun isGameWinnable(
        stock: List<Pile>,
        waste: List<Pile>,
        tableau: List<Pile>,
        freeCells: List<Pile>
    ): Boolean
}

interface PuzzleGameRules : GameRules {
    // Puzzle specific rules can be defined here
}
