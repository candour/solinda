package com.example.solinda.jewelinda

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

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
}
