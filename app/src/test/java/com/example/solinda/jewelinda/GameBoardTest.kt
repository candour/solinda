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
}
