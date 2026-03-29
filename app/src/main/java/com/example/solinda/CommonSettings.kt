package com.example.solinda

import com.google.gson.Gson
import com.example.solinda.jewelinda.LevelType

data class CommonSettings(
    val gameType: GameType,
    val dealCount: Int = 1,
    val leftMargin: Int = 20,
    val rightMargin: Int = 20,
    val leftMarginLandscape: Int = 50,
    val rightMarginLandscape: Int = 150,
    val tableauCardRevealFactor: Float = 0.2f,
    val isHapticsEnabled: Boolean = true
) {
    companion object {
        val gson = Gson()
    }
}
