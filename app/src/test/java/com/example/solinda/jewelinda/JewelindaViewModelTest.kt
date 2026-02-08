package com.example.solinda.jewelinda

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Test

class JewelindaViewModelTest {

    @Test
    fun testInitialization() {
        val viewModel = JewelindaViewModel()
        assertNotNull(viewModel.board.value)
    }

    @Test
    fun testNewGame() {
        val viewModel = JewelindaViewModel()
        val firstBoard = viewModel.board.value
        viewModel.newGame()
        val secondBoard = viewModel.board.value
        assertNotSame(firstBoard, secondBoard)
    }
}
