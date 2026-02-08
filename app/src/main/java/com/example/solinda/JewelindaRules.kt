package com.example.solinda

class JewelindaRules : GameRules {
    override val foundationPilesCount: Int = 0
    override val tableauPilesCount: Int = 0
    override val freeCellsCount: Int = 0
    override val stockPilesCount: Int = 0
    override val wastePilesCount: Int = 0

    override fun setupBoard(
        stock: List<Pile>,
        waste: List<Pile>,
        foundations: List<Pile>,
        tableau: List<Pile>,
        freeCells: List<Pile>
    ) {
        // Jewelinda doesn't use these Solitaire piles
    }

    override fun drawFromStock(stock: Pile, waste: Pile, dealCount: Int): List<Card> = emptyList()

    override fun canPlaceOnFoundation(card: Card, foundation: Pile): Boolean = false

    override fun canPlaceOnTableau(
        stack: List<Card>,
        toPile: Pile,
        freeCells: List<Pile>,
        tableau: List<Pile>
    ): Boolean = false

    override fun canPlaceOnFreeCell(stack: List<Card>, freeCell: Pile): Boolean = false

    override fun isValidTableauStack(stack: List<Card>): Boolean = false

    override fun revealIfNeeded(pile: Pile) {}

    override fun checkWin(foundations: List<Pile>): Boolean = false

    override fun isGameWinnable(
        stock: List<Pile>,
        waste: List<Pile>,
        tableau: List<Pile>,
        freeCells: List<Pile>
    ): Boolean = false
}
