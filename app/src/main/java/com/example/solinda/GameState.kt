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
    val leftMarginLandscape: Int = 20,
    val rightMarginLandscape: Int = 20,
    val tableauCardRevealFactor: Float = 0.3f,
    val isHapticsEnabled: Boolean = true
)

data class SolitaireData(
    val stock: List<PileState>,
    val waste: List<PileState>,
    val foundations: List<PileState>,
    val tableau: List<PileState>,
    val freeCells: List<PileState>
)

data class JewelindaData(
    val boardJson: String?,
    val score: Int,
    val moves: Int,
    val levelType: LevelType,
    val frostLevelJson: String?,
    val objectiveProgressJson: String?
)

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
