package com.example.solinda

import com.google.gson.Gson
import com.example.solinda.jewelinda.GemType
import com.example.solinda.jewelinda.LevelType

data class GameState(
    val commonSettings: CommonSettings,
    val solitaireData: SolitaireData?,
    val jewelindaData: JewelindaData?
) {
    companion object {
        val gson = Gson()
    }
}

data class CommonSettings(
    val gameType: GameType,
    val dealCount: Int = 1,
    val leftMargin: Int = 20,
    val rightMargin: Int = 20,
    val leftMarginLandscape: Int = 50,
    val rightMarginLandscape: Int = 150,
    val tableauCardRevealFactor: Float = 0.2f,
    val isHapticsEnabled: Boolean = true,
    val jewelindaBoardJson: String? = null,
    val jewelindaScore: Int = 0,
    val jewelindaMoves: Int = 30,
    val jewelindaLevelType: com.example.solinda.jewelinda.LevelType = com.example.solinda.jewelinda.LevelType.COLOR_COLLECTION,
    val frostLevelJson: String? = null,
    val objectiveProgressJson: String? = null
) {
    companion object {
        val gson = Gson()
    }
}

data class PileState(
    val cards: List<CardState>,
    val type: PileType
)

data class CardState(
    val suit: Suit,
    val rank: Int,
    var faceUp: Boolean = false,
    var x: Float = 0f,
    var y: Float = 0f
)
