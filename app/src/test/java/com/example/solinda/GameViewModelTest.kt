package com.example.solinda

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GameViewModelTest {

    private lateinit var viewModel: GameViewModel

    @Before
    fun setup() {
        viewModel = GameViewModel()
        viewModel.newGame()
    }

    @Test
    fun `isGameWinnable is false when game starts`() {
        assertFalse(viewModel.isGameWinnable())
    }

    @Test
    fun `isGameWinnable is true when stock and waste are empty and all tableau cards are face up`() {
        viewModel.stock.cards.clear()
        viewModel.waste.cards.clear()
        viewModel.tableau.forEach { pile -> pile.cards.forEach { card -> card.faceUp = true } }
        assertTrue(viewModel.isGameWinnable())
    }

    @Test
    fun `isGameWinnable is false when stock and waste are empty but tableau has face down cards`() {
        viewModel.stock.cards.clear()
        viewModel.waste.cards.clear()
        // Ensure at least one card is face down
        viewModel.tableau.first().cards.first().faceUp = false
        assertFalse(viewModel.isGameWinnable())
    }

    @Test
    fun `canPlaceOnFoundation is true for valid placement`() {
        val aceOfSpades = Card(Suit.SPADES, 1)
        val twoOfSpades = Card(Suit.SPADES, 2)
        val foundation = Pile(PileType.FOUNDATION)
        assertTrue(viewModel.canPlaceOnFoundation(aceOfSpades, foundation))
        foundation.addCard(aceOfSpades)
        assertTrue(viewModel.canPlaceOnFoundation(twoOfSpades, foundation))
    }

    @Test
    fun `canPlaceOnFoundation is false for invalid placement`() {
        val aceOfSpades = Card(Suit.SPADES, 1)
        val threeOfSpades = Card(Suit.SPADES, 3)
        val twoOfHearts = Card(Suit.HEARTS, 2)
        val foundation = Pile(PileType.FOUNDATION)
        foundation.addCard(aceOfSpades)
        assertFalse(viewModel.canPlaceOnFoundation(threeOfSpades, foundation))
        assertFalse(viewModel.canPlaceOnFoundation(twoOfHearts, foundation))
    }

    @Test
    fun `canPlaceOnTableau is true for valid placement`() {
        val kingOfSpades = Card(Suit.SPADES, 13)
        val queenOfHearts = Card(Suit.HEARTS, 12)
        val tableauPile = Pile(PileType.TABLEAU)
        assertTrue(viewModel.canPlaceOnTableau(listOf(kingOfSpades), tableauPile))
        tableauPile.addCard(kingOfSpades)
        assertTrue(viewModel.canPlaceOnTableau(listOf(queenOfHearts), tableauPile))
    }

    @Test
    fun `canPlaceOnTableau is false for invalid placement`() {
        val kingOfSpades = Card(Suit.SPADES, 13)
        val queenOfSpades = Card(Suit.SPADES, 12)
        val jackOfHearts = Card(Suit.HEARTS, 11)
        val tableauPile = Pile(PileType.TABLEAU)
        tableauPile.addCard(kingOfSpades)
        assertFalse(viewModel.canPlaceOnTableau(listOf(queenOfSpades), tableauPile))
        assertFalse(viewModel.canPlaceOnTableau(listOf(jackOfHearts), tableauPile))
    }
}
