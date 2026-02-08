package com.example.solinda.jewelinda

import org.junit.Assert.*
import org.junit.Test

class GameBoardTest {

    @Test
    fun testInitBoard() {
        val board = GameBoard()
        board.initBoard()

        // Check if board is full
        for (y in 0 until GameBoard.HEIGHT) {
            for (x in 0 until GameBoard.WIDTH) {
                assertNotNull("Gem at ($x, $y) should not be null", board.getGem(x, y))
            }
        }

        // Check for no initial matches
        assertFalse("Board should not have any initial matches", board.hasAnyMatch())

        // Check for at least one possible move
        assertTrue("Board should have at least one possible move", board.hasPossibleMoves())
    }

    @Test
    fun testHasAnyMatch() {
        val board = GameBoard()
        val grid = Array(GameBoard.HEIGHT) { y ->
            Array<Gem?>(GameBoard.WIDTH) { x ->
                Gem(type = GemType.RED, posX = x, posY = y)
            }
        }

        // All RED should have matches
        board.setGrid(grid)
        assertTrue(board.hasAnyMatch())

        // Alternating RED/BLUE should NOT have matches
        val alternatingGrid = Array(GameBoard.HEIGHT) { y ->
            Array<Gem?>(GameBoard.WIDTH) { x ->
                val type = if ((x + y) % 2 == 0) GemType.RED else GemType.BLUE
                Gem(type = type, posX = x, posY = y)
            }
        }
        board.setGrid(alternatingGrid)
        assertFalse(board.hasAnyMatch())
    }

    @Test
    fun testHasPossibleMoves() {
        val board = GameBoard()

        val grid = Array(GameBoard.HEIGHT) { y ->
            Array<Gem?>(GameBoard.WIDTH) { x ->
                // Pattern that ensures no matches: (x + 2*y) % 6
                val type = GemType.entries[(x + 2 * y) % 6]
                Gem(type = type, posX = x, posY = y)
            }
        }
        board.setGrid(grid)
        assertFalse("Initial pattern should not have matches", board.hasAnyMatch())

        // Setup a potential move:
        // (0,0)=T0, (1,0)=T1, (2,0)=T0, (3,0)=T0
        // Swap (0,0) with (1,0) -> (0,0)=T1, (1,0)=T0, (2,0)=T0, (3,0)=T0 -> MATCH!
        grid[0][0] = Gem(type = GemType.entries[0], posX = 0, posY = 0)
        grid[0][1] = Gem(type = GemType.entries[1], posX = 1, posY = 0)
        grid[0][2] = Gem(type = GemType.entries[0], posX = 2, posY = 0)
        grid[0][3] = Gem(type = GemType.entries[0], posX = 3, posY = 0)

        board.setGrid(grid)
        assertFalse("Should not have any match yet", board.hasAnyMatch())
        assertTrue("Should have a possible move", board.hasPossibleMoves())
    }

    @Test
    fun testFindAllMatches() {
        val board = GameBoard()
        val grid = Array(GameBoard.HEIGHT) { arrayOfNulls<Gem>(GameBoard.WIDTH) }

        // Horizontal match of 3
        grid[0][0] = Gem(type = GemType.RED, posX = 0, posY = 0)
        grid[0][1] = Gem(type = GemType.RED, posX = 1, posY = 0)
        grid[0][2] = Gem(type = GemType.RED, posX = 2, posY = 0)
        grid[0][3] = Gem(type = GemType.BLUE, posX = 3, posY = 0)

        // Vertical match of 4
        grid[2][5] = Gem(type = GemType.GREEN, posX = 5, posY = 2)
        grid[3][5] = Gem(type = GemType.GREEN, posX = 5, posY = 3)
        grid[4][5] = Gem(type = GemType.GREEN, posX = 5, posY = 4)
        grid[5][5] = Gem(type = GemType.GREEN, posX = 5, posY = 5)

        board.setGrid(grid)
        val matches = board.findAllMatches()

        assertEquals(7, matches.size)
        assertTrue(matches.contains(Pair(0, 0)))
        assertTrue(matches.contains(Pair(1, 0)))
        assertTrue(matches.contains(Pair(2, 0)))
        assertTrue(matches.contains(Pair(5, 2)))
        assertTrue(matches.contains(Pair(5, 3)))
        assertTrue(matches.contains(Pair(5, 4)))
        assertTrue(matches.contains(Pair(5, 5)))
    }

    @Test
    fun testShiftGemsDown() {
        val board = GameBoard()
        val grid = Array(GameBoard.HEIGHT) { arrayOfNulls<Gem>(GameBoard.WIDTH) }

        // Gem at (0,0) with null below it
        grid[0][0] = Gem(type = GemType.RED, posX = 0, posY = 0)
        // Gem at (5,5) with null below it at (5,7)
        grid[5][5] = Gem(type = GemType.BLUE, posX = 5, posY = 5)
        grid[6][5] = null
        grid[7][5] = null

        board.setGrid(grid)
        board.shiftGemsDown()

        assertNull(board.getGem(0, 0))
        val fallenGem1 = board.getGem(0, GameBoard.HEIGHT - 1)
        assertNotNull(fallenGem1)
        assertEquals(GemType.RED, fallenGem1?.type)
        assertEquals(GameBoard.HEIGHT - 1, fallenGem1?.posY)

        assertNull(board.getGem(5, 5))
        val fallenGem2 = board.getGem(5, 7)
        assertNotNull(fallenGem2)
        assertEquals(GemType.BLUE, fallenGem2?.type)
        assertEquals(7, fallenGem2?.posY)
    }

    @Test
    fun testSwapGems() {
        val board = GameBoard()
        val grid = Array(GameBoard.HEIGHT) { arrayOfNulls<Gem>(GameBoard.WIDTH) }
        val gem1 = Gem(type = GemType.RED, posX = 0, posY = 0)
        val gem2 = Gem(type = GemType.BLUE, posX = 1, posY = 0)
        grid[0][0] = gem1
        grid[0][1] = gem2
        board.setGrid(grid)

        board.swapGems(0, 0, 1, 0)

        assertEquals(GemType.BLUE, board.getGem(0, 0)?.type)
        assertEquals(GemType.RED, board.getGem(1, 0)?.type)
        assertEquals(0, board.getGem(0, 0)?.posX)
        assertEquals(1, board.getGem(1, 0)?.posX)
        // Check IDs remained the same
        assertEquals(gem2.id, board.getGem(0, 0)?.id)
        assertEquals(gem1.id, board.getGem(1, 0)?.id)
    }
}
