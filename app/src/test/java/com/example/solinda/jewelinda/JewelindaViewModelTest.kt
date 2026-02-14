package com.example.solinda.jewelinda

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.example.solinda.GameState
import com.example.solinda.GameType
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
    }

    @Test
    fun testInitialization() {
        val viewModel = JewelindaViewModel(application)
        assertNotNull(viewModel.board.value)
    }

    @Test
    fun testNewGame() {
        val viewModel = JewelindaViewModel(application)
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

        val gameState = GameState(
            stock = emptyList(), waste = emptyList(), foundations = emptyList(),
            tableau = emptyList(), freeCells = emptyList(), gameType = GameType.JEWELINDA,
            jewelindaBoardJson = gson.toJson(board.getGridFlattened()),
            frostLevelJson = gson.toJson(frost),
            jewelindaMoves = 30, jewelindaScore = 0
        )
        val stateJson = gson.toJson(gameState)

        // Mock shared preferences to return our frosted board state
        whenever(sharedPreferences.getString(anyString(), org.mockito.kotlin.anyOrNull())).thenReturn(stateJson)

        val viewModel = JewelindaViewModel(application)

        // Check if frost is loaded
        assert(viewModel.board.value.getFrostLevel(2, 2) > 0)

        // Swiping (2,2) should be blocked
        viewModel.onSwipe(2, 2, Direction.EAST)
        assertFalse("Should not be processing after swiping frosted gem", viewModel.isProcessing.value)

        // Swiping onto (2,2) from (2,1) should be blocked
        viewModel.onSwipe(2, 1, Direction.SOUTH)
        assertFalse("Should not be processing after swiping onto frosted gem", viewModel.isProcessing.value)
    }
}
