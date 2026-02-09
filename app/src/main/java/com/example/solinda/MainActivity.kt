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
import android.widget.CheckBox
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.compose.ui.platform.ComposeView
import com.example.solinda.jewelinda.JewelindaViewModel
import com.example.solinda.jewelinda.ui.JewelindaScreen
import com.example.solinda.jewelinda.ui.JewelindaTheme

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()
    private val jewelindaViewModel: JewelindaViewModel by viewModels()
    private lateinit var gameView: GameView
    private lateinit var jewelindaComposeView: ComposeView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("solinda_prefs", Context.MODE_PRIVATE)
        viewModel.loadGame(prefs)

        val frameLayout = FrameLayout(this)

        gameView = GameView(this, viewModel)
        frameLayout.addView(gameView)

        jewelindaComposeView = ComposeView(this).apply {
            setContent {
                JewelindaTheme {
                    JewelindaScreen(viewModel = jewelindaViewModel, gameViewModel = viewModel)
                }
            }
        }
        frameLayout.addView(jewelindaComposeView)

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
                if (viewModel.gameType == GameType.JEWELINDA) {
                    jewelindaViewModel.newGame()
                } else {
                    viewModel.newGame()
                    gameView.invalidate() // Redraw the view
                }
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

        setContentView(frameLayout)
        updateGameVisibility()
    }

    private fun updateGameVisibility() {
        if (viewModel.gameType == GameType.JEWELINDA) {
            gameView.visibility = View.GONE
            jewelindaComposeView.visibility = View.VISIBLE
        } else {
            gameView.visibility = View.VISIBLE
            jewelindaComposeView.visibility = View.GONE
        }
    }

    private fun showOptionsDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Options")

        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        val mainLayout = LinearLayout(this).apply {
            orientation = if (isLandscape) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL
            setPadding(100, 50, 100, 50)
        }

        val leftColumn: LinearLayout
        val rightColumn: LinearLayout

        if (isLandscape) {
            leftColumn = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setPadding(0, 0, 50, 0)
            }
            rightColumn = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setPadding(50, 0, 0, 0)
            }
            mainLayout.addView(leftColumn)
            mainLayout.addView(rightColumn)
        } else {
            leftColumn = mainLayout
            rightColumn = mainLayout
        }

        // Game Type Spinner
        val gameTypeLabel = TextView(this).apply {
            text = "Game Type"
            setPadding(0, 0, 0, 16)
        }
        leftColumn.addView(gameTypeLabel)

        val gameTypeSpinner = Spinner(this)
        val gameTypes = GameType.entries.map { it.name.lowercase().replaceFirstChar { char -> char.uppercase() } }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, gameTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        gameTypeSpinner.adapter = adapter
        gameTypeSpinner.setSelection(viewModel.gameType.ordinal)
        leftColumn.addView(gameTypeSpinner)

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
        leftColumn.addView(radioGroup)

        // Left Margin
        val leftMarginLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, if (isLandscape) 0 else 50, 0, 0)
        }
        val leftMarginLabel = TextView(this).apply { text = "Left Margin (dp)" }
        leftMarginLayout.addView(leftMarginLabel)
        val leftMarginInput = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(if (isLandscape) viewModel.leftMarginLandscape.toString() else viewModel.leftMargin.toString())
        }
        leftMarginLayout.addView(leftMarginInput)
        rightColumn.addView(leftMarginLayout)

        // Right Margin
        val rightMarginLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 20, 0, 0)
        }
        val rightMarginLabel = TextView(this).apply { text = "Right Margin (dp)" }
        rightMarginLayout.addView(rightMarginLabel)
        val rightMarginInput = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(if (isLandscape) viewModel.rightMarginLandscape.toString() else viewModel.rightMargin.toString())
        }
        rightMarginLayout.addView(rightMarginInput)
        rightColumn.addView(rightMarginLayout)

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
        rightColumn.addView(revealFactorLayout)

        // Haptic Feedback Toggle
        val hapticFeedbackCheckBox = CheckBox(this).apply {
            text = "Haptic Feedback"
            isChecked = viewModel.isHapticsEnabled
            setPadding(0, 20, 0, 0)
        }
        rightColumn.addView(hapticFeedbackCheckBox)

        builder.setView(mainLayout)

        builder.setPositiveButton("Save") { dialog, _ ->
            val selectedGameType = GameType.entries[gameTypeSpinner.selectedItemPosition]
            val selectedDealCount = radioGroup.checkedRadioButtonId
            val newLeftMargin = leftMarginInput.text.toString().toIntOrNull() ?: if (isLandscape) viewModel.leftMarginLandscape else viewModel.leftMargin
            val newRightMargin = rightMarginInput.text.toString().toIntOrNull() ?: if (isLandscape) viewModel.rightMarginLandscape else viewModel.rightMargin
            val newRevealFactor = revealFactorInput.text.toString().toFloatOrNull() ?: viewModel.tableauCardRevealFactor
            val newHapticsEnabled = hapticFeedbackCheckBox.isChecked

            val marginsChanged = if (isLandscape) {
                viewModel.leftMarginLandscape != newLeftMargin || viewModel.rightMarginLandscape != newRightMargin
            } else {
                viewModel.leftMargin != newLeftMargin || viewModel.rightMargin != newRightMargin
            }

            val gameSettingsChanged = viewModel.gameType != selectedGameType ||
                    viewModel.dealCount != selectedDealCount ||
                    marginsChanged ||
                    viewModel.tableauCardRevealFactor != newRevealFactor ||
                    viewModel.isHapticsEnabled != newHapticsEnabled

            if (gameSettingsChanged) {
                // Apply settings
                if (isLandscape) {
                    viewModel.leftMarginLandscape = newLeftMargin
                    viewModel.rightMarginLandscape = newRightMargin
                } else {
                    viewModel.leftMargin = newLeftMargin
                    viewModel.rightMargin = newRightMargin
                }
                viewModel.tableauCardRevealFactor = newRevealFactor
                viewModel.isHapticsEnabled = newHapticsEnabled
                viewModel.resetGame(selectedGameType, selectedDealCount)
                updateGameVisibility()
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
