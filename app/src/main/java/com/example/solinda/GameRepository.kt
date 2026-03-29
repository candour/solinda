package com.example.solinda

import android.content.Context
import android.content.SharedPreferences

class GameRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("solinda_prefs", Context.MODE_PRIVATE)

    fun saveGame(gameState: GameState) {
        val json = GameState.gson.toJson(gameState)
        prefs.edit().putString("game_state", json).apply()
    }

    fun loadGame(): GameState? {
        val json = prefs.getString("game_state", null)
        return if (json != null) {
            try {
                GameState.gson.fromJson(json, GameState::class.java)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
}
