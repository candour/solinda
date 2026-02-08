package com.example.solinda.jewelinda

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class JewelindaViewModel : ViewModel() {
    private val _board = MutableStateFlow(GameBoard())
    val board: StateFlow<GameBoard> = _board.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    init {
        newGame()
    }

    fun newGame() {
        val newBoard = GameBoard()
        newBoard.initBoard()
        _board.value = newBoard
    }

    fun onSwipe(x: Int, y: Int, direction: Direction) {
        if (_isProcessing.value) return
        // Phase 3: Implement swap and match logic
    }
}
