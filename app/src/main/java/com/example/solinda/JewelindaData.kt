package com.example.solinda

import com.example.solinda.jewelinda.LevelType

data class JewelindaData(
    val boardJson: String?,
    val score: Int,
    val moves: Int,
    val levelType: LevelType,
    val frostLevelJson: String?,
    val objectiveProgressJson: String?
)
