package com.example.solinda.jewelinda.ui

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.solinda.GameViewModel
import com.example.solinda.jewelinda.JewelindaEvent
import com.example.solinda.jewelinda.JewelindaViewModel
import com.example.solinda.jewelinda.ParticleViewModel

@Composable
fun JewelindaScreen(viewModel: JewelindaViewModel, gameViewModel: GameViewModel) {
    val score by viewModel.score.collectAsState()
    val moves by viewModel.movesRemaining.collectAsState()
    val particleViewModel: ParticleViewModel = viewModel()
    val view = LocalView.current

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            if (event is JewelindaEvent.MatchPerformed && gameViewModel.isHapticsEnabled) {
                val hapticType = when {
                    event.size >= 5 -> HapticFeedbackConstants.LONG_PRESS
                    event.size >= 4 -> HapticFeedbackConstants.KEYBOARD_TAP
                    else -> HapticFeedbackConstants.VIRTUAL_KEY
                }
                view.performHapticFeedback(hapticType)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Score: $score",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Target: ${JewelindaViewModel.TARGET_SCORE}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Moves",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = "$moves",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (moves <= 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            }

            GameGrid(viewModel = viewModel, particleViewModel = particleViewModel)
        }

        if (moves <= 0) {
            GameOverOverlay(
                score = score,
                targetScore = JewelindaViewModel.TARGET_SCORE,
                onPlayAgain = { viewModel.newGame() }
            )
        }
    }
}
