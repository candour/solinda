package com.example.solinda

data class CalculatorData(
    val displayText: String = "0",
    val memoryValue: Double = 0.0,
    val storedValue: Double? = null,
    val pendingOperator: String? = null,
    val isNewInput: Boolean = true
)
