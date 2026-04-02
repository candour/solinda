package com.example.solinda.calculator

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CalculatorViewModelTest {

    private lateinit var viewModel: CalculatorViewModel

    @Before
    fun setup() {
        viewModel = CalculatorViewModel()
    }

    @Test
    fun testAddition() {
        viewModel.onNumberClick("5")
        viewModel.onOperatorClick("+")
        viewModel.onNumberClick("3")
        viewModel.onEqualsClick()
        assertEquals("8", viewModel.displayText)
    }

    @Test
    fun testSubtraction() {
        viewModel.onNumberClick("1")
        viewModel.onNumberClick("0")
        viewModel.onOperatorClick("-")
        viewModel.onNumberClick("4")
        viewModel.onEqualsClick()
        assertEquals("6", viewModel.displayText)
    }

    @Test
    fun testMultiplication() {
        viewModel.onNumberClick("6")
        viewModel.onOperatorClick("*")
        viewModel.onNumberClick("7")
        viewModel.onEqualsClick()
        assertEquals("42", viewModel.displayText)
    }

    @Test
    fun testDivision() {
        viewModel.onNumberClick("1")
        viewModel.onNumberClick("5")
        viewModel.onOperatorClick("/")
        viewModel.onNumberClick("3")
        viewModel.onEqualsClick()
        assertEquals("5", viewModel.displayText)
    }

    @Test
    fun testDivisionByZero() {
        viewModel.onNumberClick("5")
        viewModel.onOperatorClick("/")
        viewModel.onNumberClick("0")
        viewModel.onEqualsClick()
        assertEquals("Error", viewModel.displayText)
    }

    @Test
    fun testDecimal() {
        viewModel.onNumberClick("5")
        viewModel.onDecimalClick()
        viewModel.onNumberClick("2")
        assertEquals("5.2", viewModel.displayText)
        viewModel.onOperatorClick("+")
        viewModel.onNumberClick("0")
        viewModel.onDecimalClick()
        viewModel.onNumberClick("8")
        viewModel.onEqualsClick()
        assertEquals("6", viewModel.displayText)
    }

    @Test
    fun testAC() {
        viewModel.onNumberClick("5")
        viewModel.onOperatorClick("+")
        viewModel.onACClick()
        assertEquals("0", viewModel.displayText)
        assertNull(viewModel.storedValue)
    }

    @Test
    fun testMemory() {
        viewModel.onNumberClick("1")
        viewModel.onNumberClick("0")
        viewModel.onMemoryAdd() // Memory = 10
        viewModel.onACClick()
        viewModel.onNumberClick("5")
        viewModel.onMemorySubtract() // Memory = 10 - 5 = 5
        viewModel.onACClick()
        viewModel.onMemoryRecall()
        assertEquals("5", viewModel.displayText)
        viewModel.onMemoryClear()
        viewModel.onMemoryRecall()
        assertEquals("0", viewModel.displayText)
    }

    @Test
    fun testPercentage() {
        viewModel.onNumberClick("5")
        viewModel.onNumberClick("0")
        viewModel.onPercentageClick()
        assertEquals("0.5", viewModel.displayText)
    }

    @Test
    fun testPlusMinus() {
        viewModel.onNumberClick("5")
        viewModel.onPlusMinusClick()
        assertEquals("-5", viewModel.displayText)
        viewModel.onPlusMinusClick()
        assertEquals("5", viewModel.displayText)
    }

    @Test
    fun testSequentialCalculations() {
        // First calculation: 5 + 3 = 8
        viewModel.onNumberClick("5")
        viewModel.onOperatorClick("+")
        viewModel.onNumberClick("3")
        viewModel.onEqualsClick()
        assertEquals("8", viewModel.displayText)

        // Second calculation starting after equals: 10 + 2 = 12
        viewModel.onNumberClick("1")
        viewModel.onNumberClick("0")
        viewModel.onOperatorClick("+")
        viewModel.onNumberClick("2")
        viewModel.onEqualsClick()
        assertEquals("12", viewModel.displayText)
    }
}
