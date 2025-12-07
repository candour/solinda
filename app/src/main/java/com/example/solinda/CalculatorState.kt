package com.example.solinda

data class CalculatorState(
    val display: String = "0",
    val memory: Double = 0.0,
    val currentOperator: String? = null,
    val previousValue: Double = 0.0,
    val overwriteDisplay: Boolean = true
)
