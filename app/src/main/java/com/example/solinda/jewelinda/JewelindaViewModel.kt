package com.example.solinda.jewelinda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class JewelindaViewModel : ViewModel() {
    private val _board = MutableStateFlow(GameBoard())
    val board: StateFlow<GameBoard> = _board.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    init {
        newGame()
    }

    fun newGame() {
        val newBoard = GameBoard()
        newBoard.initBoard()
        _board.value = newBoard
        _score.value = 0
    }

    fun onSwipe(x: Int, y: Int, direction: Direction) {
        if (_isProcessing.value) return

        val targetX = when (direction) {
            Direction.EAST -> x + 1
            Direction.WEST -> x - 1
            else -> x
        }
        val targetY = when (direction) {
            Direction.NORTH -> y - 1
            Direction.SOUTH -> y + 1
            else -> y
        }

        if (targetX in 0 until GameBoard.WIDTH && targetY in 0 until GameBoard.HEIGHT) {
            viewModelScope.launch {
                processMove(y, x, targetY, targetX)
            }
        }
    }

    private suspend fun processMove(row1: Int, col1: Int, row2: Int, col2: Int) {
        _isProcessing.value = true

        val boardCopy = _board.value.copy()
        boardCopy.swapGems(col1, row1, col2, row2)
        _board.value = boardCopy
        delay(300)

        if (boardCopy.hasAnyMatch()) {
            var multiplier = 1
            while (boardCopy.hasAnyMatch()) {
                val clearedCount = boardCopy.findAndRemoveMatches()
                _score.value += clearedCount * 50 * multiplier
                _board.value = boardCopy.copy()
                delay(300)

                boardCopy.shiftGemsDown()
                _board.value = boardCopy.copy()
                delay(300)

                boardCopy.refillBoard()
                _board.value = boardCopy.copy()
                delay(300)

                multiplier *= 2
            }
        } else {
            // Swap back
            boardCopy.swapGems(col1, row1, col2, row2)
            _board.value = boardCopy.copy()
            delay(300)
        }

        _isProcessing.value = false
    }
}
