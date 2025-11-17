package com.example.solinda

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

        val frameLayout = FrameLayout(this)

        val gameView = GameView(this, viewModel)
        frameLayout.addView(gameView)

        val newGameButton = Button(this).apply {
            text = "New Game"
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                bottomMargin = 32
            }
            setOnClickListener {
                viewModel.newGame()
                gameView.invalidate() // Redraw the view
            }
        }
        frameLayout.addView(newGameButton)

        setContentView(frameLayout)
    }
}
