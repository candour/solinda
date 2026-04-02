package com.example.solinda.calculator

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.solinda.*

class CalculatorViewModel : ViewModel() {

    var displayText by mutableStateOf("0")
    var memoryValue by mutableStateOf(0.0)
    var storedValue by mutableStateOf<Double?>(null)
    var pendingOperator by mutableStateOf<String?>(null)
    var isNewInput by mutableStateOf(true)

    fun onNumberClick(number: String) {
        if (isNewInput) {
            if (pendingOperator == null) {
                storedValue = null
            }
            displayText = number
            isNewInput = false
        } else {
            if (displayText == "0") {
                displayText = number
            } else {
                displayText += number
            }
        }
    }

    fun onOperatorClick(operator: String) {
        val currentValue = displayText.toDoubleOrNull() ?: 0.0
        if (storedValue == null || (pendingOperator == null && isNewInput)) {
            storedValue = currentValue
        } else if (pendingOperator != null && !isNewInput) {
            calculateResult()
        }
        pendingOperator = operator
        isNewInput = true
    }

    fun onEqualsClick() {
        calculateResult()
        pendingOperator = null
        isNewInput = true
    }

    private fun calculateResult() {
        val currentValue = displayText.toDoubleOrNull() ?: 0.0
        val baseValue = storedValue ?: return
        val result = when (pendingOperator) {
            "+" -> baseValue + currentValue
            "-" -> baseValue - currentValue
            "*" -> baseValue * currentValue
            "/" -> if (currentValue != 0.0) baseValue / currentValue else Double.NaN
            else -> currentValue
        }
        displayText = formatResult(result)
        storedValue = result
    }

    private fun formatResult(result: Double): String {
        return if (result.isNaN()) "Error"
        else if (result == result.toLong().toDouble()) result.toLong().toString()
        else result.toString()
    }

    fun onACClick() {
        displayText = "0"
        storedValue = null
        pendingOperator = null
        isNewInput = true
    }

    fun onPlusMinusClick() {
        val currentValue = displayText.toDoubleOrNull() ?: 0.0
        displayText = formatResult(currentValue * -1)
    }

    fun onPercentageClick() {
        val currentValue = displayText.toDoubleOrNull() ?: 0.0
        displayText = formatResult(currentValue / 100.0)
    }

    fun onDecimalClick() {
        if (isNewInput) {
            displayText = "0."
            isNewInput = false
        } else if (!displayText.contains(".")) {
            displayText += "."
        }
    }

    fun onMemoryAdd() {
        memoryValue += (displayText.toDoubleOrNull() ?: 0.0)
        isNewInput = true
    }

    fun onMemorySubtract() {
        memoryValue -= (displayText.toDoubleOrNull() ?: 0.0)
        isNewInput = true
    }

    fun onMemoryRecall() {
        displayText = formatResult(memoryValue)
        isNewInput = true
    }

    fun onMemoryClear() {
        memoryValue = 0.0
    }

    fun saveGame(repository: GameRepository) {
        val existingGameState = repository.loadGame()
        val calculatorData = CalculatorData(
            displayText = displayText,
            memoryValue = memoryValue,
            storedValue = storedValue,
            pendingOperator = pendingOperator,
            isNewInput = isNewInput
        )
        val updatedGameState = existingGameState?.copy(calculatorData = calculatorData)
            ?: GameState(
                commonSettings = CommonSettings(gameType = GameType.CALCULATOR),
                solitaireData = null,
                jewelindaData = null,
                calculatorData = calculatorData
            )
        repository.saveGame(updatedGameState)
    }

    fun loadGame(repository: GameRepository) {
        val gameState = repository.loadGame()
        gameState?.calculatorData?.let { data ->
            displayText = data.displayText
            memoryValue = data.memoryValue
            storedValue = data.storedValue
            pendingOperator = data.pendingOperator
            isNewInput = data.isNewInput
        }
    }
}
