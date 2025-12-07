package com.example.solinda

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.text.InputType
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()
    private lateinit var gameView: GameView
    private lateinit var calculatorView: CalculatorView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("solinda_prefs", Context.MODE_PRIVATE)
        viewModel.loadGame(prefs)

        val frameLayout = FrameLayout(this)

        gameView = GameView(this, viewModel)
        frameLayout.addView(gameView)

        calculatorView = CalculatorView(this, viewModel)
        frameLayout.addView(calculatorView)

        if (viewModel.gameType == GameType.CALCULATOR) {
            gameView.visibility = View.GONE
            calculatorView.visibility = View.VISIBLE
        } else {
            gameView.visibility = View.VISIBLE
            calculatorView.visibility = View.GONE
        }

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

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(100, 50, 100, 50)
        }

        // Game Type Spinner
        val gameTypeLabel = TextView(this).apply {
            text = "Game Type"
            setPadding(0, 0, 0, 16)
        }
        layout.addView(gameTypeLabel)

        val gameTypeSpinner = Spinner(this)
        val gameTypes = GameType.entries.map { it.name.lowercase().replaceFirstChar { char -> char.uppercase() } }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, gameTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        gameTypeSpinner.adapter = adapter
        gameTypeSpinner.setSelection(viewModel.gameType.ordinal)
        layout.addView(gameTypeSpinner)

        // Deal Count Radio Group
        val radioGroup = RadioGroup(this).apply {
            orientation = RadioGroup.VERTICAL
            setPadding(0, 50, 0, 0)
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
        layout.addView(radioGroup)

        // Left Margin
        val leftMarginLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 50, 0, 0)
        }
        val leftMarginLabel = TextView(this).apply { text = "Left Margin (dp)" }
        leftMarginLayout.addView(leftMarginLabel)
        val leftMarginInput = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(viewModel.leftMargin.toString())
        }
        leftMarginLayout.addView(leftMarginInput)
        layout.addView(leftMarginLayout)


        // Right Margin
        val rightMarginLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 20, 0, 0)
        }
        val rightMarginLabel = TextView(this).apply { text = "Right Margin (dp)" }
        rightMarginLayout.addView(rightMarginLabel)
        val rightMarginInput = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(viewModel.rightMargin.toString())
        }
        rightMarginLayout.addView(rightMarginInput)
        layout.addView(rightMarginLayout)

        // Tableau Card Reveal Factor
        val revealFactorLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 20, 0, 0)
        }
        val revealFactorLabel = TextView(this).apply { text = "Tableau Reveal (0.1-1.0)" }
        revealFactorLayout.addView(revealFactorLabel)
        val revealFactorInput = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(viewModel.tableauCardRevealFactor.toString())
        }
        revealFactorLayout.addView(revealFactorInput)
        layout.addView(revealFactorLayout)


        builder.setView(layout)

        builder.setPositiveButton("Save") { dialog, _ ->
            val selectedGameType = GameType.entries[gameTypeSpinner.selectedItemPosition]
            val selectedDealCount = radioGroup.checkedRadioButtonId
            val newLeftMargin = leftMarginInput.text.toString().toIntOrNull() ?: viewModel.leftMargin
            val newRightMargin = rightMarginInput.text.toString().toIntOrNull() ?: viewModel.rightMargin
            val newRevealFactor = revealFactorInput.text.toString().toFloatOrNull() ?: viewModel.tableauCardRevealFactor

            val gameSettingsChanged = viewModel.gameType != selectedGameType ||
                    viewModel.dealCount != selectedDealCount ||
                    viewModel.leftMargin != newLeftMargin ||
                    viewModel.rightMargin != newRightMargin ||
                    viewModel.tableauCardRevealFactor != newRevealFactor

            if (selectedGameType == GameType.CALCULATOR) {
                // Switch to calculator view
                calculatorView.visibility = View.VISIBLE
                gameView.visibility = View.GONE
                viewModel.gameType = selectedGameType
            } else if (gameSettingsChanged) {
                // Switch to game view and apply settings
                calculatorView.visibility = View.GONE
                gameView.visibility = View.VISIBLE
                viewModel.leftMargin = newLeftMargin
                viewModel.rightMargin = newRightMargin
                viewModel.tableauCardRevealFactor = newRevealFactor
                viewModel.resetGame(selectedGameType, selectedDealCount)
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
