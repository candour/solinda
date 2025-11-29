package com.example.solinda

interface GameRules {
    val foundationPilesCount: Int
    val tableauPilesCount: Int

    fun setupBoard(stock: Pile, waste: Pile, foundations: List<Pile>, tableau: List<Pile>)
    fun drawFromStock(stock: Pile, waste: Pile, dealCount: Int): List<Card>
    fun canPlaceOnFoundation(card: Card, foundation: Pile): Boolean
    fun canPlaceOnTableau(stack: List<Card>, tableauPile: Pile): Boolean
    fun revealIfNeeded(pile: Pile)
    fun checkWin(foundations: List<Pile>): Boolean
}
