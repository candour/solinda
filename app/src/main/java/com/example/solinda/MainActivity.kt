package com.example.solinda

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()
    private lateinit var gameView: GameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("solinda_prefs", Context.MODE_PRIVATE)
        viewModel.loadGame(prefs)

        val frameLayout = FrameLayout(this)

        gameView = GameView(this, viewModel)
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

        val optionsButton = Button(this).apply {
            text = "Options"
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.TOP or Gravity.END
                topMargin = 192
                marginEnd = 32
            }
            setOnClickListener {
                showOptionsDialog()
            }
        }
        frameLayout.addView(optionsButton)

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            optionsButton.visibility = View.GONE
        }

        setContentView(frameLayout)
    }

    private fun showOptionsDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Options")

        val radioGroup = RadioGroup(this).apply {
            orientation = RadioGroup.VERTICAL
            setPadding(100, 50, 100, 50)
        }

        val deal1 = RadioButton(this).apply {
            text = "Deal 1 Card"
            id = 1
        }
        radioGroup.addView(deal1)

        val deal3 = RadioButton(this).apply {
            text = "Deal 3 Cards"
            id = 3
        }
        radioGroup.addView(deal3)

        radioGroup.check(viewModel.dealCount)

        builder.setView(radioGroup)

        builder.setPositiveButton("Save") { dialog, _ ->
            val selectedId = radioGroup.checkedRadioButtonId
            if (viewModel.dealCount != selectedId) {
                viewModel.dealCount = selectedId
                viewModel.newGame()
                gameView.invalidate()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.create().show()
    }

    override fun onStop() {
        super.onStop()
        val prefs = getSharedPreferences("solinda_prefs", Context.MODE_PRIVATE)
        viewModel.saveGame(prefs)
    }
}
