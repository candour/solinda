package com.example.solinda

class FreeCellRules : GameRules {
    override val foundationPilesCount: Int = 4
    override val tableauPilesCount: Int = 8
    override val freeCellsCount: Int = 4
    override val stockPilesCount: Int = 0
    override val wastePilesCount: Int = 0


    override fun setupBoard(
        stock: List<Pile>,
        waste: List<Pile>,
        foundations: List<Pile>,
        tableau: List<Pile>,
        freeCells: List<Pile>
    ) {
        // Clear all piles
        stock.forEach { it.cards.clear() }
        waste.forEach { it.cards.clear() }
        foundations.forEach { it.cards.clear() }
        tableau.forEach { it.cards.clear() }
        freeCells.forEach { it.cards.clear() }

        val deck = mutableListOf<Card>()
        for (suit in Suit.entries) {
            for (rank in 1..13) deck.add(Card(suit, rank))
        }
        deck.shuffle()

        // Deal tableau
        var currentPile = 0
        for (card in deck) {
            card.faceUp = true
            tableau[currentPile].addCard(card)
            currentPile = (currentPile + 1) % tableauPilesCount
        }
    }

    override fun drawFromStock(stock: Pile, waste: Pile, dealCount: Int): List<Card> {
        // Not applicable to FreeCell
        return emptyList()
    }

    override fun canPlaceOnFoundation(card: Card, foundation: Pile): Boolean {
        val top = foundation.topCard()
        return when {
            foundation.isEmpty() -> card.rank == 1
            top != null && top.suit == card.suit && card.rank == top.rank + 1 -> true
            else -> false
        }
    }

    override fun canPlaceOnTableau(stack: List<Card>, tableauPile: Pile): Boolean {
        if (stack.isEmpty()) return false
        val top = tableauPile.topCard()
        val bottomCard = stack.first()
        return when {
            tableauPile.isEmpty() -> true
            top != null && top.color != bottomCard.color && bottomCard.rank == top.rank - 1 -> true
            else -> false
        }
    }

    override fun canPlaceOnFreeCell(stack: List<Card>, freeCell: Pile): Boolean {
        return freeCell.isEmpty() && stack.size == 1
    }


    override fun revealIfNeeded(pile: Pile) {
        // Not applicable to FreeCell, all cards are dealt face up
    }

    override fun checkWin(foundations: List<Pile>): Boolean {
        return foundations.all { it.cards.size == 13 }
    }

    override fun isGameWinnable(
        stock: List<Pile>,
        waste: List<Pile>,
        tableau: List<Pile>,
        freeCells: List<Pile>
    ): Boolean {
        // For now, we'll consider the game always winnable until a more complex algorithm is needed.
        return true
    }
}
