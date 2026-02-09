package com.example.solinda.jewelinda

class GameBoard {
    companion object {
        const val WIDTH = 8
        const val HEIGHT = 8
    }

    private var grid: Array<Array<Gem?>> = Array(HEIGHT) { arrayOfNulls<Gem>(WIDTH) }

    // Internal for testing
    internal fun setGrid(newGrid: Array<Array<Gem?>>) {
        this.grid = newGrid
    }

    fun copy(): GameBoard {
        val newBoard = GameBoard()
        val newGrid = Array(HEIGHT) { y ->
            Array(WIDTH) { x ->
                grid[y][x]
            }
        }
        newBoard.setGrid(newGrid)
        return newBoard
    }

    fun getGem(x: Int, y: Int): Gem? {
        if (x !in 0 until WIDTH || y !in 0 until HEIGHT) return null
        return grid[y][x]
    }

    fun initBoard() {
        do {
            for (y in 0 until HEIGHT) {
                for (x in 0 until WIDTH) {
                    var type: GemType
                    do {
                        type = GemType.entries.random()
                    } while (createsMatchAt(x, y, type))
                    grid[y][x] = Gem(type = type, posX = x, posY = y)
                }
            }
        } while (!hasPossibleMoves())
    }

    private fun createsMatchAt(x: Int, y: Int, type: GemType): Boolean {
        // Check horizontal match (to the left)
        if (x >= 2 && grid[y][x - 1]?.type == type && grid[y][x - 2]?.type == type) return true
        // Check vertical match (upwards)
        if (y >= 2 && grid[y - 1][x]?.type == type && grid[y - 2][x]?.type == type) return true
        return false
    }

    fun hasAnyMatch(): Boolean {
        for (y in 0 until HEIGHT) {
            for (x in 0 until WIDTH) {
                if (hasMatchAt(x, y)) return true
            }
        }
        return false
    }

    fun hasPossibleMoves(): Boolean {
        for (y in 0 until HEIGHT) {
            for (x in 0 until WIDTH) {
                // Try swapping with right neighbor
                if (x + 1 < WIDTH) {
                    if (isSwapValid(x, y, x + 1, y)) return true
                }
                // Try swapping with bottom neighbor
                if (y + 1 < HEIGHT) {
                    if (isSwapValid(x, y, x, y + 1)) return true
                }
            }
        }
        return false
    }

    private fun isSwapValid(x1: Int, y1: Int, x2: Int, y2: Int): Boolean {
        val gem1 = grid[y1][x1] ?: return false
        val gem2 = grid[y2][x2] ?: return false
        if (gem1.type == gem2.type) return false

        // Perform temporary swap
        grid[y1][x1] = gem2
        grid[y2][x2] = gem1

        val match = hasMatchAt(x1, y1) || hasMatchAt(x2, y2)

        // Swap back
        grid[y1][x1] = gem1
        grid[y2][x2] = gem2

        return match
    }

    private fun hasMatchAt(x: Int, y: Int): Boolean {
        val type = grid[y][x]?.type ?: return false

        // Horizontal check
        var horizontalCount = 1
        var ix = x - 1
        while (ix >= 0 && grid[y][ix]?.type == type) {
            horizontalCount++
            ix--
        }
        ix = x + 1
        while (ix < WIDTH && grid[y][ix]?.type == type) {
            horizontalCount++
            ix++
        }
        if (horizontalCount >= 3) return true

        // Vertical check
        var verticalCount = 1
        var iy = y - 1
        while (iy >= 0 && grid[iy][x]?.type == type) {
            verticalCount++
            iy--
        }
        iy = y + 1
        while (iy < HEIGHT && grid[iy][x]?.type == type) {
            verticalCount++
            iy++
        }
        if (verticalCount >= 3) return true

        return false
    }

    fun swapGems(x1: Int, y1: Int, x2: Int, y2: Int) {
        val gem1 = grid[y1][x1]
        val gem2 = grid[y2][x2]

        grid[y1][x1] = gem2?.copy(posX = x1, posY = y1)
        grid[y2][x2] = gem1?.copy(posX = x2, posY = y2)
    }

    fun findAllMatches(): Set<Pair<Int, Int>> {
        val matches = mutableSetOf<Pair<Int, Int>>()
        // Horizontal
        for (y in 0 until HEIGHT) {
            var x = 0
            while (x < WIDTH) {
                val type = grid[y][x]?.type
                if (type != null) {
                    var count = 1
                    while (x + count < WIDTH && grid[y][x + count]?.type == type) {
                        count++
                    }
                    if (count >= 3) {
                        for (i in 0 until count) {
                            matches.add(Pair(x + i, y))
                        }
                    }
                    x += count
                } else {
                    x++
                }
            }
        }
        // Vertical
        for (x in 0 until WIDTH) {
            var y = 0
            while (y < HEIGHT) {
                val type = grid[y][x]?.type
                if (type != null) {
                    var count = 1
                    while (y + count < HEIGHT && grid[y + count][x]?.type == type) {
                        count++
                    }
                    if (count >= 3) {
                        for (i in 0 until count) {
                            matches.add(Pair(x, y + i))
                        }
                    }
                    y += count
                } else {
                    y++
                }
            }
        }
        return matches
    }

    fun findAndRemoveMatches(): Int {
        val matches = findAllMatches()
        if (matches.isEmpty()) return 0
        for ((x, y) in matches) {
            grid[y][x] = null
        }
        return matches.size
    }

    fun shiftGemsDown() {
        for (x in 0 until WIDTH) {
            var emptyRow = HEIGHT - 1
            for (y in HEIGHT - 1 downTo 0) {
                if (grid[y][x] != null) {
                    if (y != emptyRow) {
                        grid[emptyRow][x] = grid[y][x]?.copy(posX = x, posY = emptyRow)
                        grid[y][x] = null
                    }
                    emptyRow--
                }
            }
        }
    }

    fun refillBoard() {
        for (x in 0 until WIDTH) {
            for (y in 0 until HEIGHT) {
                if (grid[y][x] == null) {
                    grid[y][x] = Gem(type = GemType.entries.random(), posX = x, posY = y)
                }
            }
        }
    }

    fun shuffleBoard() {
        val allGems = grid.flatten().filterNotNull().toMutableList()
        do {
            allGems.shuffle()
            var index = 0
            for (y in 0 until HEIGHT) {
                for (x in 0 until WIDTH) {
                    val gem = allGems[index++]
                    grid[y][x] = gem.copy(posX = x, posY = y)
                }
            }
        } while (hasAnyMatch() || !hasPossibleMoves())
    }
}
