package com.example.solinda

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FreeCellRulesTest {

    private lateinit var rules: FreeCellRules
    private lateinit var freeCells: List<Pile>
    private lateinit var foundations: List<Pile>
    private lateinit var tableau: List<Pile>

    @Before
    fun setup() {
        rules = FreeCellRules()
        freeCells = List(4) { Pile(PileType.FREE_CELL) }
        foundations = List(4) { Pile(PileType.FOUNDATION) }
        tableau = List(8) { Pile(PileType.TABLEAU) }
    }

    @Test
    fun `canPlaceOnFreeCell is true for single card and empty free cell`() {
        val card = Card(Suit.SPADES, 1)
        assertTrue(rules.canPlaceOnFreeCell(listOf(card), freeCells.first()))
    }

    @Test
    fun `canPlaceOnFreeCell is false for multiple cards`() {
        val card1 = Card(Suit.SPADES, 1)
        val card2 = Card(Suit.SPADES, 2)
        assertFalse(rules.canPlaceOnFreeCell(listOf(card1, card2), freeCells.first()))
    }

    @Test
    fun `canPlaceOnFreeCell is false for non-empty free cell`() {
        val card1 = Card(Suit.SPADES, 1)
        val card2 = Card(Suit.SPADES, 2)
        val freeCell = freeCells.first()
        freeCell.addCard(card1)
        assertFalse(rules.canPlaceOnFreeCell(listOf(card2), freeCell))
    }

    @Test
    fun `canPlaceOnTableau is true for any card on empty tableau`() {
        val card = Card(Suit.SPADES, 10)
        assertTrue(rules.canPlaceOnTableau(listOf(card), tableau.first()))
    }

    @Test
    fun `canPlaceOnTableau is true for valid alternating color and descending rank`() {
        val tenOfSpades = Card(Suit.SPADES, 10)
        val nineOfHearts = Card(Suit.HEARTS, 9)
        val tableauPile = tableau.first()
        tableauPile.addCard(tenOfSpades)
        assertTrue(rules.canPlaceOnTableau(listOf(nineOfHearts), tableauPile))
    }

    @Test
    fun `canPlaceOnTableau is false for same color`() {
        val tenOfSpades = Card(Suit.SPADES, 10)
        val nineOfClubs = Card(Suit.CLUBS, 9)
        val tableauPile = tableau.first()
        tableauPile.addCard(tenOfSpades)
        assertFalse(rules.canPlaceOnTableau(listOf(nineOfClubs), tableauPile))
    }

    @Test
    fun `canPlaceOnTableau is false for non-descending rank`() {
        val tenOfSpades = Card(Suit.SPADES, 10)
        val nineOfHearts = Card(Suit.HEARTS, 9)
        val tableauPile = tableau.first()
        tableauPile.addCard(nineOfHearts)
        assertFalse(rules.canPlaceOnTableau(listOf(tenOfSpades), tableauPile))
    }

    @Test
    fun `checkWin is true when all foundations are full`() {
        for (foundation in foundations) {
            for (i in 1..13) {
                foundation.addCard(Card(Suit.SPADES, i))
            }
        }
        assertTrue(rules.checkWin(foundations))
    }

    @Test
    fun `checkWin is false when not all foundations are full`() {
        for (i in 1..12) {
            foundations.first().addCard(Card(Suit.SPADES, i))
        }
        assertFalse(rules.checkWin(foundations))
    }
}
