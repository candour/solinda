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
    data class MatchPerformed(val size: Int, val isFrostCleared: Boolean = false) : JewelindaEvent()
    data object Shuffle : JewelindaEvent()
    data object BombExploded : JewelindaEvent()
}

class JewelindaViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val TARGET_SCORE = 1000
        const val INITIAL_MOVES = 30
    }

    var soundManager: SoundManager? = null

    private val _board = MutableStateFlow(GameBoard())
    val board: StateFlow<GameBoard> = _board.asStateFlow()

    private val _levelType = MutableStateFlow(LevelType.COLOR_COLLECTION)
    val levelType: StateFlow<LevelType> = _levelType.asStateFlow()

    private val _objectives = MutableStateFlow<Map<GemType, Int>>(emptyMap())
    val objectives: StateFlow<Map<GemType, Int>> = _objectives.asStateFlow()

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
        val frostJson = gson.toJson(_board.value.getFrostLevelsFlattened())
        val objectiveJson = gson.toJson(_objectives.value)

        val updatedGameState = gameState.copy(
            jewelindaBoardJson = boardJson,
            jewelindaScore = _score.value,
            jewelindaMoves = _movesRemaining.value,
            jewelindaLevelType = _levelType.value,
            frostLevelJson = frostJson,
            objectiveProgressJson = objectiveJson
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

                    gameState.frostLevelJson?.let { fJson ->
                        val frost: Array<IntArray> = gson.fromJson(fJson, Array<IntArray>::class.java)
                        loadedBoard.loadFrost(frost)
                    }

                    _board.value = loadedBoard
                    _score.value = gameState.jewelindaScore
                    _movesRemaining.value = gameState.jewelindaMoves
                    _levelType.value = gameState.jewelindaLevelType

                    gameState.objectiveProgressJson?.let { oJson ->
                        val objType = object : TypeToken<Map<GemType, Int>>() {}.type
                        val objectives: Map<GemType, Int> = gson.fromJson(oJson, objType)
                        _objectives.value = objectives
                    }
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

        val type = LevelType.entries.random()
        _levelType.value = type

        if (type == LevelType.FROST_CLEARANCE || type == LevelType.HYBRID) {
            newBoard.initFrost()
        }

        if (type == LevelType.COLOR_COLLECTION || type == LevelType.HYBRID) {
            val colors = GemType.entries.shuffled().take(2)
            _objectives.value = colors.associateWith { 25 }
        } else {
            _objectives.value = emptyMap()
        }

        _board.value = newBoard
        _score.value = 0
        _movesRemaining.value = INITIAL_MOVES
        saveGame()
    }

    fun checkWinCondition(): Boolean {
        val objectivesMet = _objectives.value.values.all { it <= 0 }

        val frostMet = if (_levelType.value == LevelType.FROST_CLEARANCE || _levelType.value == LevelType.HYBRID) {
            val board = _board.value
            var allClear = true
            outer@ for (y in 0 until GameBoard.HEIGHT) {
                for (x in 0 until GameBoard.WIDTH) {
                    if (board.getFrostLevel(x, y) > 0) {
                        allClear = false
                        break@outer
                    }
                }
            }
            allClear
        } else {
            true
        }

        return objectivesMet && frostMet
    }

    fun onSwipe(x: Int, y: Int, direction: Direction) {
        if (_isProcessing.value) return

        val board = _board.value
        if (board.getFrostLevel(x, y) > 0) return

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
            if (board.getFrostLevel(targetX, targetY) > 0) return

            viewModelScope.launch {
                processMove(y, x, targetY, targetX)
            }
        }
    }

    private suspend fun processMove(row1: Int, col1: Int, row2: Int, col2: Int) {
        if (_movesRemaining.value <= 0) return
        _isProcessing.value = true
        _isGravityEnabled.value = false

        val swapTarget = Pair(col2, row2)
        val boardCopy = _board.value.copy()
        boardCopy.swapGems(col1, row1, col2, row2)
        _board.value = boardCopy
        delay(600)

        if (boardCopy.hasAnyMatch()) {
            _movesRemaining.value -= 1
            var multiplier = 1
            var isInitialMove = true
            while (boardCopy.hasAnyMatch()) {
                val matchGroups = boardCopy.findAllMatchGroups()
                val matchedCoords = matchGroups.flatMap { it.gems }.toSet()

                // Identify bombs to be created
                val newBombs = mutableListOf<Triple<Int, Int, GemType>>()
                for (group in matchGroups) {
                    if (group.gems.size >= 4) {
                        val bombPos = if (isInitialMove && group.gems.contains(swapTarget)) {
                            swapTarget
                        } else {
                            group.gems.minWithOrNull(compareBy({ it.second }, { it.first }))!!
                        }
                        newBombs.add(Triple(bombPos.first, bombPos.second, group.type))
                    }
                }

                // Identify triggered bombs (matched bombs or hit by explosion)
                val allClearedCoords = matchedCoords.toMutableSet()
                val bombsToTrigger = matchedCoords.filter { boardCopy.getGem(it.first, it.second)?.isBomb == true }.toMutableList()
                val triggeredBombs = mutableSetOf<Pair<Int, Int>>()

                var bIdx = 0
                while (bIdx < bombsToTrigger.size) {
                    val (bx, by) = bombsToTrigger[bIdx]
                    if (triggeredBombs.add(Pair(bx, by))) {
                        _events.emit(JewelindaEvent.BombExploded)
                        val area = boardCopy.getExplosionArea(bx, by)
                        for (coord in area) {
                            val gem = boardCopy.getGem(coord.first, coord.second)
                            if (gem != null) {
                                if (allClearedCoords.add(coord)) {
                                    // Gem cleared by explosion
                                }
                                if (gem.isBomb && !triggeredBombs.contains(coord)) {
                                    bombsToTrigger.add(coord)
                                }
                            }
                        }
                    }
                    bIdx++
                }

                var frostClearedInThisStep = false
                val currentObjectives = _objectives.value.toMutableMap()

                allClearedCoords.forEach { (x, y) ->
                    boardCopy.getGem(x, y)?.let { gem ->
                        _events.emit(JewelindaEvent.GemCleared(x, y, gem.type))
                        if (currentObjectives.containsKey(gem.type)) {
                            currentObjectives[gem.type] = (currentObjectives[gem.type]!! - 1).coerceAtLeast(0)
                        }
                    }
                    if (boardCopy.decrementFrost(x, y)) {
                        frostClearedInThisStep = true
                    }
                }
                _objectives.value = currentObjectives

                if (frostClearedInThisStep) {
                    soundManager?.playIceCrack()
                }

                _events.emit(JewelindaEvent.MatchPerformed(allClearedCoords.size, frostClearedInThisStep))

                val clearedCount = allClearedCoords.size
                boardCopy.removeGems(allClearedCoords)

                // Place new bombs
                for ((bx, by, bType) in newBombs) {
                    boardCopy.setBomb(bx, by, bType)
                }

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
                isInitialMove = false
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
