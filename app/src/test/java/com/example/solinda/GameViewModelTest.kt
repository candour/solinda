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
    fun `canPlaceOnFreeCell is true for valid placement`() {
        viewModel.initializeGameType(GameType.FREECELL)
        val aceOfSpades = Card(Suit.SPADES, 1)
        val freeCell = viewModel.freeCells[0]
        assertTrue(viewModel.canPlaceOnFreeCell(listOf(aceOfSpades), freeCell))
    }

    @Test
    fun `canPlaceOnFreeCell is false for invalid placement`() {
        viewModel.initializeGameType(GameType.FREECELL)
        val aceOfSpades = Card(Suit.SPADES, 1)
        val twoOfSpades = Card(Suit.SPADES, 2)
        val freeCell = viewModel.freeCells[0]
        freeCell.addCard(aceOfSpades)
        assertFalse(viewModel.canPlaceOnFreeCell(listOf(twoOfSpades), freeCell))
        assertFalse(viewModel.canPlaceOnFreeCell(listOf(aceOfSpades, twoOfSpades), freeCell))
    }

    @Test
    fun `initializeGameType sets the correct game type`() {
        viewModel.initializeGameType(GameType.FREECELL)
        assertEquals(GameType.FREECELL, viewModel.gameType)
    }

    @Test
    fun `autoMoveCard prioritizes longest tableau stack`() {
        viewModel.initializeGameType(GameType.KLONDIKE)

        // Clear all piles for a clean slate
        viewModel.tableau.forEach { it.cards.clear() }

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
        val card1 = Card(Suit.DIAMONDS, 6)
        card1.faceUp = true
        val card2 = Card(Suit.SPADES, 5)
        card2.faceUp = true
        longStack.addCard(card1)
        longStack.addCard(card2) // Valid destination for Heart 4

        // Act
        val targetPile = viewModel.autoMoveCard(cardToMove, fromPile)

        // Assert
        assertEquals("Card should move to the longest stack", longStack, targetPile)
        assertEquals("Longest stack should now have 3 cards", 3, longStack.cards.size)
        assertTrue("Card to move should be at the top of the longest stack", longStack.topCard() == cardToMove)
        assertTrue("Source pile should be empty", fromPile.isEmpty())
        assertTrue("Short stack should be unchanged", shortStack.cards.size == 1)
    }

    @Test
    fun `autoMoveCard prioritizes tableau over foundation in Klondike`() {
        viewModel.initializeGameType(GameType.KLONDIKE)
        viewModel.tableau.forEach { it.cards.clear() }
        viewModel.foundations.forEach { it.cards.clear() }

        val cardToMove = Card(Suit.SPADES, 1) // Ace of Spades
        val fromPile = viewModel.tableau[0]
        fromPile.addCard(cardToMove)

        // Tableau destination: Empty pile for King, but let's use a different card for a valid tableau move
        val cardToMove2 = Card(Suit.HEARTS, 4)
        fromPile.cards.clear()
        fromPile.addCard(cardToMove2)

        // Tableau destination: 5 of Clubs
        val tableauPile = viewModel.tableau[1]
        val fiveOfClubs = Card(Suit.CLUBS, 5)
        fiveOfClubs.faceUp = true
        tableauPile.addCard(fiveOfClubs)

        // Foundation destination: Empty foundation (not valid for 4 of Hearts)
        // Let's use 3 of Hearts on foundation
        val foundationPile = viewModel.foundations[0]
        foundationPile.addCard(Card(Suit.HEARTS, 1))
        foundationPile.addCard(Card(Suit.HEARTS, 2))
        foundationPile.addCard(Card(Suit.HEARTS, 3))

        // Act
        val targetPile = viewModel.autoMoveCard(cardToMove2, fromPile)

        // Assert
        assertEquals("Should move to Tableau, not Foundation", PileType.TABLEAU, targetPile?.type)
        assertEquals(tableauPile, targetPile)
    }

    @Test
    fun `autoMoveCard does not move to foundation in FreeCell`() {
        viewModel.initializeGameType(GameType.FREECELL)
        viewModel.tableau.forEach { it.cards.clear() }
        viewModel.foundations.forEach { it.cards.clear() }

        val aceOfSpades = Card(Suit.SPADES, 1)
        val fromPile = viewModel.tableau[0]
        fromPile.addCard(aceOfSpades)

        // In FreeCell, autoMoveCard will move to tableau if a spot is available.
        // To test it returns null, we should ensure no valid tableau move exists.
        // An empty tableau pile is a valid move for any card in FreeCell.
        // So let's fill all tableau piles with something that can't take an Ace.
        viewModel.tableau.forEach { it.addCard(Card(Suit.SPADES, 10)) }
        fromPile.cards.clear()
        fromPile.addCard(aceOfSpades)

        // Act
        val targetPile = viewModel.autoMoveCard(aceOfSpades, fromPile)

        // Assert
        assertEquals("Should not move to Foundation in FreeCell via autoMoveCard", null, targetPile)
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
