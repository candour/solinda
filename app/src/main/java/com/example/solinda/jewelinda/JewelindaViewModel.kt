package com.example.solinda.jewelinda

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.solinda.GameState
import com.example.solinda.GameType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class JewelindaEvent {
    data class GemCleared(val x: Int, val y: Int, val type: GemType) : JewelindaEvent()
    data class MatchPerformed(val size: Int) : JewelindaEvent()
    data object Shuffle : JewelindaEvent()
}

class JewelindaViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val TARGET_SCORE = 1000
        const val INITIAL_MOVES = 30
    }

    private val _board = MutableStateFlow(GameBoard())
    val board: StateFlow<GameBoard> = _board.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _isGravityEnabled = MutableStateFlow(false)
    val isGravityEnabled: StateFlow<Boolean> = _isGravityEnabled.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _movesRemaining = MutableStateFlow(INITIAL_MOVES)
    val movesRemaining: StateFlow<Int> = _movesRemaining.asStateFlow()

    private val _events = MutableSharedFlow<JewelindaEvent>()
    val events: SharedFlow<JewelindaEvent> = _events.asSharedFlow()

    init {
        loadGame()
    }

    fun saveGame() {
        val prefs = getApplication<Application>().getSharedPreferences("solinda_prefs", Context.MODE_PRIVATE)
        val json = prefs.getString("game_state", null)
        val gson = Gson()
        val gameState = if (json != null) {
            try {
                gson.fromJson(json, GameState::class.java)
            } catch (e: Exception) {
                createDefaultGameState()
            }
        } else {
            createDefaultGameState()
        }

        val boardJson = gson.toJson(_board.value.getGridFlattened())
        val updatedGameState = gameState.copy(
            jewelindaBoardJson = boardJson,
            jewelindaScore = _score.value,
            jewelindaMoves = _movesRemaining.value
        )

        prefs.edit().putString("game_state", gson.toJson(updatedGameState)).apply()
    }

    fun loadGame() {
        val prefs = getApplication<Application>().getSharedPreferences("solinda_prefs", Context.MODE_PRIVATE)
        val json = prefs.getString("game_state", null)
        if (json != null) {
            try {
                val gson = Gson()
                val gameState = gson.fromJson(json, GameState::class.java)
                val boardJson = gameState.jewelindaBoardJson
                if (boardJson != null) {
                    val gemListType = object : TypeToken<List<Gem>>() {}.type
                    val gems: List<Gem> = gson.fromJson(boardJson, gemListType)
                    val loadedBoard = GameBoard()
                    loadedBoard.loadGrid(gems)
                    _board.value = loadedBoard
                    _score.value = gameState.jewelindaScore
                    _movesRemaining.value = gameState.jewelindaMoves
                    return
                }
            } catch (e: Exception) {
                // Fallback to newGame
            }
        }
        newGame()
    }

    private fun createDefaultGameState(): GameState {
        return GameState(
            stock = emptyList(),
            waste = emptyList(),
            foundations = emptyList(),
            tableau = emptyList(),
            freeCells = emptyList(),
            gameType = GameType.JEWELINDA
        )
    }

    fun newGame() {
        val newBoard = GameBoard()
        newBoard.initBoard()
        _board.value = newBoard
        _score.value = 0
        _movesRemaining.value = INITIAL_MOVES
        saveGame()
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
        if (_movesRemaining.value <= 0) return
        _isProcessing.value = true
        _isGravityEnabled.value = false

        val boardCopy = _board.value.copy()
        boardCopy.swapGems(col1, row1, col2, row2)
        _board.value = boardCopy
        delay(600)

        if (boardCopy.hasAnyMatch()) {
            _movesRemaining.value -= 1
            var multiplier = 1
            while (boardCopy.hasAnyMatch()) {
                val matches = boardCopy.findAllMatches()
                _events.emit(JewelindaEvent.MatchPerformed(matches.size))
                matches.forEach { (x, y) ->
                    boardCopy.getGem(x, y)?.let { gem ->
                        _events.emit(JewelindaEvent.GemCleared(x, y, gem.type))
                    }
                }

                val clearedCount = boardCopy.findAndRemoveMatches()
                _score.value += clearedCount * 50 * multiplier
                _board.value = boardCopy.copy()
                delay(400)

                _isGravityEnabled.value = true
                boardCopy.refillAndPrepareFall()
                _board.value = boardCopy.copy()
                delay(50)

                boardCopy.finalizeFall()
                _board.value = boardCopy.copy()
                delay(400)

                multiplier *= 2
            }

            // Check if shuffle needed
            if (!boardCopy.hasPossibleMoves()) {
                delay(500)
                boardCopy.shuffleBoard()
                _events.emit(JewelindaEvent.Shuffle)
                _board.value = boardCopy.copy()
                delay(600)
            }
            saveGame()
        } else {
            // Swap back
            boardCopy.swapGems(col1, row1, col2, row2)
            _board.value = boardCopy.copy()
            delay(600)
        }

        _isProcessing.value = false
    }
}
