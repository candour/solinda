package com.example.solinda

import com.google.gson.Gson

data class GameState(
    val commonSettings: CommonSettings,
    val solitaireData: SolitaireData?,
    val jewelindaData: JewelindaData?,
    val calculatorData: CalculatorData? = null
) {
    companion object {
        val gson = Gson()
    }
}
