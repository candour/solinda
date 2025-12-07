package com.example.solinda

class KlondikeRules : GameRules {
    override val foundationPilesCount: Int = 4
    override val tableauPilesCount: Int = 7
    override val freeCellsCount: Int = 0
    override val stockPilesCount: Int = 1
    override val wastePilesCount: Int = 1

    override fun setupBoard(
        stock: List<Pile>,
        waste: List<Pile>,
        foundations: List<Pile>,
        tableau: List<Pile>,
        freeCells: List<Pile>
    ) {
        // Clear all piles
        stock.first().cards.clear()
        waste.first().cards.clear()
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

        stock.first().cards.addAll(deck)
    }

    override fun drawFromStock(stock: Pile, waste: Pile, dealCount: Int): List<Card> {
        val drawnCards = mutableListOf<Card>()
        if (stock.cards.isEmpty()) {
            // Recycle waste into stock
            stock.cards.addAll(waste.cards.map { it.copy(faceUp = false) }.asReversed())
            waste.cards.clear()
        } else {
            val count = stock.cards.size.coerceAtMost(dealCount)
            for (i in 0 until count) {
                val card = stock.removeTopCard()!!
                card.faceUp = true
                waste.addCard(card)
                drawnCards.add(card)
            }
        }
        return drawnCards
    }

    override fun canPlaceOnFoundation(card: Card, foundation: Pile): Boolean {
        val top = foundation.topCard()
        return when {
            foundation.isEmpty() -> card.rank == 1
            top != null && top.suit == card.suit && card.rank == top.rank + 1 -> true
            else -> false
        }
    }

    override fun canPlaceOnTableau(stack: List<Card>, toPile: Pile, freeCells: List<Pile>, tableau: List<Pile>): Boolean {
        if (stack.isEmpty()) return false
        val top = toPile.topCard()
        val bottomCard = stack.first()
        return when {
            toPile.isEmpty() -> bottomCard.rank == 13
            top != null && top.color != bottomCard.color && bottomCard.rank == top.rank - 1 -> true
            else -> false
        }
    }

    override fun canPlaceOnFreeCell(stack: List<Card>, freeCell: Pile): Boolean {
        return false
    }

    override fun isValidTableauStack(stack: List<Card>): Boolean {
        if (stack.isEmpty()) return false
        for (i in 0 until stack.size - 1) {
            val top = stack[i]
            val bottom = stack[i+1]
            if (top.color == bottom.color || top.rank != bottom.rank + 1) {
                return false
            }
        }
        return true
    }

    override fun revealIfNeeded(pile: Pile) {
        if (pile.type == PileType.TABLEAU && pile.topCard()?.faceUp == false) {
            pile.topCard()?.faceUp = true
        }
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
        return stock.first().cards.isEmpty() && waste.first().cards.isEmpty() && tableau.all { pile -> pile.cards.all { card -> card.faceUp } }
    }
}
