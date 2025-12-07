package com.example.solinda

import org.junit.Assert.assertEquals
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
        viewModel.stock.first().cards.clear()
        viewModel.waste.first().cards.clear()
        viewModel.tableau.forEach { pile -> pile.cards.forEach { card -> card.faceUp = true } }
        assertTrue(viewModel.isGameWinnable())
    }

    @Test
    fun `isGameWinnable is false when stock and waste are empty but tableau has face down cards`() {
        viewModel.stock.first().cards.clear()
        viewModel.waste.first().cards.clear()
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

    @Test
    fun `initializeGameType sets the correct game type`() {
        viewModel.initializeGameType(GameType.FREECELL)
        assertEquals(GameType.FREECELL, viewModel.gameType)
    }

    @Test
    fun `autoMoveCard prioritizes longest tableau stack`() {
        viewModel.initializeGameType(GameType.FREECELL)

        // Clear all piles for a clean slate
        viewModel.tableau.forEach { it.cards.clear() }
        viewModel.freeCells.forEach { it.cards.clear() }

        val cardToMove = Card(Suit.HEARTS, 4)
        val fromPile = viewModel.tableau[0]
        fromPile.addCard(cardToMove)

        // Destination 1: Empty pile (lowest priority)
        val emptyPile = viewModel.tableau[1] // Stays empty

        // Destination 2: Shorter valid stack
        val shortStack = viewModel.tableau[2]
        shortStack.addCard(Card(Suit.CLUBS, 5)) // Valid destination for Heart 4

        // Destination 3: Longest valid stack (highest priority)
        val longStack = viewModel.tableau[3]
        longStack.addCard(Card(Suit.DIAMONDS, 6))
        longStack.addCard(Card(Suit.SPADES, 5)) // Valid destination for Heart 4

        // Act
        val targetPile = viewModel.autoMoveCard(cardToMove, fromPile)

        // Assert
        assertEquals("Card should move to the longest stack", longStack, targetPile)
        assertEquals("Longest stack should now have 3 cards", 3, longStack.cards.size)
        assertTrue("Card to move should be at the top of the longest stack", longStack.topCard() == cardToMove)
        assertTrue("Source pile should be empty", fromPile.isEmpty())
        assertTrue("Short stack should be unchanged", shortStack.cards.size == 1)
        assertTrue("Empty pile should remain empty", emptyPile.isEmpty())
    }

    @Test
    fun `autoMoveToFoundation checks free cells`() {
        viewModel.initializeGameType(GameType.FREECELL)
        val aceOfSpades = Card(Suit.SPADES, 1)
        viewModel.freeCells[0].addCard(aceOfSpades)

        viewModel.autoMoveToFoundation()
        assertEquals(aceOfSpades, viewModel.foundations[0].topCard())
    }

    @Test
    fun `findValidSubStack returns full stack for valid selection`() {
        val pile = Pile(PileType.TABLEAU)
        pile.addCard(Card(Suit.SPADES, 5))
        pile.addCard(Card(Suit.HEARTS, 4))
        pile.addCard(Card(Suit.CLUBS, 3))
        val result = viewModel.findValidSubStack(pile, 0)
        assertEquals(3, result.size)
    }

    @Test
    fun `findValidSubStack returns valid sub-stack for invalid selection`() {
        val pile = Pile(PileType.TABLEAU)
        pile.addCard(Card(Suit.SPADES, 10))
        pile.addCard(Card(Suit.HEARTS, 5))
        pile.addCard(Card(Suit.CLUBS, 4))
        val result = viewModel.findValidSubStack(pile, 0)
        assertEquals(2, result.size)
        assertEquals(5, result.first().rank)
    }

    @Test
    fun `findValidSubStack returns single card when no valid sub-stack exists`() {
        val pile = Pile(PileType.TABLEAU)
        pile.addCard(Card(Suit.SPADES, 10))
        pile.addCard(Card(Suit.HEARTS, 8))
        pile.addCard(Card(Suit.CLUBS, 5))
        val result = viewModel.findValidSubStack(pile, 0)
        assertEquals(1, result.size)
        assertEquals(5, result.first().rank)
    }

    @Test
    fun `findValidSubStack works for FreeCell`() {
        viewModel.initializeGameType(GameType.FREECELL)
        val pile = Pile(PileType.TABLEAU)
        pile.addCard(Card(Suit.SPADES, 10))
        pile.addCard(Card(Suit.HEARTS, 5))
        pile.addCard(Card(Suit.CLUBS, 4))
        val result = viewModel.findValidSubStack(pile, 0)
        assertEquals(2, result.size)
        assertEquals(5, result.first().rank)
    }
}

class CalculatorViewModelTest {

    private lateinit var viewModel: GameViewModel

    @Before
    fun setup() {
        viewModel = GameViewModel()
    }

    @Test
    fun `handleDigit appends digits correctly`() {
        viewModel.onCalculatorInput("1")
        viewModel.onCalculatorInput("2")
        assertEquals("12", viewModel.calculatorState.display)
    }

    @Test
    fun `addition works correctly`() {
        viewModel.onCalculatorInput("5")
        viewModel.onCalculatorInput("+")
        viewModel.onCalculatorInput("3")
        viewModel.onCalculatorInput("=")
        assertEquals("8.0", viewModel.calculatorState.display)
    }

    @Test
    fun `subtraction works correctly`() {
        viewModel.onCalculatorInput("5")
        viewModel.onCalculatorInput("-")
        viewModel.onCalculatorInput("3")
        viewModel.onCalculatorInput("=")
        assertEquals("2.0", viewModel.calculatorState.display)
    }

    @Test
    fun `multiplication works correctly`() {
        viewModel.onCalculatorInput("5")
        viewModel.onCalculatorInput("*")
        viewModel.onCalculatorInput("3")
        viewModel.onCalculatorInput("=")
        assertEquals("15.0", viewModel.calculatorState.display)
    }

    @Test
    fun `division works correctly`() {
        viewModel.onCalculatorInput("6")
        viewModel.onCalculatorInput("/")
        viewModel.onCalculatorInput("3")
        viewModel.onCalculatorInput("=")
        assertEquals("2.0", viewModel.calculatorState.display)
    }

    @Test
    fun `percentage works correctly`() {
        viewModel.onCalculatorInput("50")
        viewModel.onCalculatorInput("%")
        assertEquals("0.5", viewModel.calculatorState.display)
    }

    @Test
    fun `square root works correctly`() {
        viewModel.onCalculatorInput("9")
        viewModel.onCalculatorInput("√")
        assertEquals("3.0", viewModel.calculatorState.display)
    }

    @Test
    fun `memory functions work correctly`() {
        viewModel.onCalculatorInput("5")
        viewModel.onCalculatorInput("M+")
        viewModel.onCalculatorInput("AC")
        viewModel.onCalculatorInput("MR")
        assertEquals("5.0", viewModel.calculatorState.display)
        viewModel.onCalculatorInput("3")
        viewModel.onCalculatorInput("M-")
        viewModel.onCalculatorInput("AC")
        viewModel.onCalculatorInput("MR")
        assertEquals("2.0", viewModel.calculatorState.display)
        viewModel.onCalculatorInput("MC")
        viewModel.onCalculatorInput("AC")
        viewModel.onCalculatorInput("MR")
        assertEquals("0.0", viewModel.calculatorState.display)
    }
}
