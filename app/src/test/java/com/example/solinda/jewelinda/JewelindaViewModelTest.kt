package com.example.solinda.jewelinda

import android.app.Application
import android.content.SharedPreferences
import com.example.solinda.GameState
import com.example.solinda.GameType
import com.example.solinda.CommonSettings
import com.example.solinda.JewelindaData
import com.example.solinda.GameRepository
import com.google.gson.Gson
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class JewelindaViewModelTest {

    private lateinit var application: Application
    private lateinit var repository: GameRepository
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor

    @Before
    fun setup() {
        sharedPreferencesEditor = mock {
            on { putString(anyString(), anyString()) } doReturn it
        }
        sharedPreferences = mock {
            on { getString(anyString(), org.mockito.kotlin.anyOrNull()) } doReturn null
            on { edit() } doReturn sharedPreferencesEditor
        }
        application = mock {
            on { getSharedPreferences(anyString(), anyInt()) } doReturn sharedPreferences
        }
        repository = GameRepository(application)
    }

    @Test
    fun testInitialization() {
        val viewModel = JewelindaViewModel(application)
        viewModel.loadGame(repository)
        assertNotNull(viewModel.board.value)
    }

    @Test
    fun testNewGame() {
        val viewModel = JewelindaViewModel(application)
        viewModel.newGame()
        val firstBoard = viewModel.board.value
        viewModel.newGame()
        val secondBoard = viewModel.board.value
        assertNotSame(firstBoard, secondBoard)
    }

    @Test
    fun testOnSwipeWithFrost() {
        val gson = Gson()
        val board = GameBoard()
        board.initBoard()
        // Frost at (2,2)
        val frost = Array(8) { IntArray(8) }
        frost[2][2] = 1

        val jewelindaData = JewelindaData(
            boardJson = gson.toJson(board.getGridFlattened()),
            score = 0,
            moves = 30,
            levelType = LevelType.COLOR_COLLECTION,
            frostLevelJson = gson.toJson(frost),
            objectiveProgressJson = null
        )

        val gameState = GameState(
            commonSettings = CommonSettings(gameType = GameType.JEWELINDA),
            solitaireData = null,
            jewelindaData = jewelindaData
        )
        val stateJson = gson.toJson(gameState)

        // Mock shared preferences to return our frosted board state
        whenever(sharedPreferences.getString(anyString(), org.mockito.kotlin.anyOrNull())).thenReturn(stateJson)

        val viewModel = JewelindaViewModel(application)
        viewModel.loadGame(repository)

        // Check if frost is loaded
        assert(viewModel.board.value.getFrostLevel(2, 2) > 0)

        // Swiping (2,2) should be blocked
        viewModel.onSwipe(2, 2, Direction.EAST, repository)
        assertFalse("Should not be processing after swiping frosted gem", viewModel.isProcessing.value)

        // Swiping onto (2,2) from (2,1) should be blocked
        viewModel.onSwipe(2, 1, Direction.SOUTH, repository)
        assertFalse("Should not be processing after swiping onto frosted gem", viewModel.isProcessing.value)
    }
}
