package com.example.solinda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.solinda.calculator.CalculatorViewModel
import com.example.solinda.calculator.ui.CalculatorScreen
import com.example.solinda.compass.CompassViewModel
import com.example.solinda.compass.ui.CompassScreen
import com.example.solinda.jewelinda.JewelindaViewModel
import com.example.solinda.jewelinda.ui.JewelindaScreen
import com.example.solinda.jewelinda.ui.JewelindaTheme
import com.example.solinda.ui.SolitaireScreen
import com.example.solinda.ui.SettingsScreen

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()
    private val jewelindaViewModel: JewelindaViewModel by viewModels()
    private val compassViewModel: CompassViewModel by viewModels()
    private val calculatorViewModel: CalculatorViewModel by viewModels()
    private lateinit var repository: GameRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = GameRepository(this)

        viewModel.loadGame(repository)
        jewelindaViewModel.loadGame(repository)
        calculatorViewModel.loadGame(repository)

        setContent {
            var showSettings by remember { mutableStateOf(false) }

            JewelindaTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (showSettings) {
                        SettingsScreen(
                            viewModel = viewModel,
                            repository = repository,
                            onClose = { showSettings = false }
                        )
                    } else {
                        when (viewModel.gameType) {
                            GameType.JEWELINDA -> {
                                JewelindaScreen(
                                    viewModel = jewelindaViewModel,
                                    gameViewModel = viewModel,
                                    repository = repository,
                                    onOptionsClick = { showSettings = true }
                                )
                            }
                            GameType.COMPASS -> {
                                CompassScreen(
                                    viewModel = compassViewModel,
                                    gameViewModel = viewModel,
                                    onOptionsClick = { showSettings = true }
                                )
                            }
                            GameType.CALCULATOR -> {
                                CalculatorScreen(
                                    viewModel = calculatorViewModel,
                                    gameViewModel = viewModel,
                                    onOptionsClick = { showSettings = true }
                                )
                            }
                            else -> {
                                SolitaireScreen(
                                    viewModel = viewModel,
                                    repository = repository,
                                    onOptionsClick = { showSettings = true }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.saveGame(repository)
        jewelindaViewModel.saveGame(repository)
        calculatorViewModel.saveGame(repository)
    }
}
