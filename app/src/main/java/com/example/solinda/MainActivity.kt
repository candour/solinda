package com.example.solinda

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.viewModels

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("solinda_prefs", Context.MODE_PRIVATE)
        viewModel.loadGame(prefs)

        val frameLayout = FrameLayout(this)

        val gameView = GameView(this, viewModel)
        frameLayout.addView(gameView)

        val newGameButton = Button(this).apply {
            text = "New Game"
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.TOP or Gravity.END
                topMargin = 64
                marginEnd = 32
            }
            setOnClickListener {
                viewModel.newGame()
                gameView.invalidate() // Redraw the view
            }
        }
        frameLayout.addView(newGameButton)

        setContentView(frameLayout)
    }

    override fun onStop() {
        super.onStop()
        val prefs = getSharedPreferences("solinda_prefs", Context.MODE_PRIVATE)
        viewModel.saveGame(prefs)
    }
}
