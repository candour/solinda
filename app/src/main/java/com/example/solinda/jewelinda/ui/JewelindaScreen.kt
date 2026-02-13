package com.example.solinda.jewelinda.ui

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
fun JewelindaScreen(
    viewModel: JewelindaViewModel,
    gameViewModel: GameViewModel,
    onOptionsClick: () -> Unit
) {
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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isLandscape = maxWidth > maxHeight

            if (isLandscape) {
                LandscapeLayout(
                    score = score,
                    moves = moves,
                    viewModel = viewModel,
                    particleViewModel = particleViewModel,
                    isHapticsEnabled = gameViewModel.isHapticsEnabled,
                    onOptionsClick = onOptionsClick
                )
            } else {
                PortraitLayout(
                    score = score,
                    moves = moves,
                    viewModel = viewModel,
                    particleViewModel = particleViewModel,
                    isHapticsEnabled = gameViewModel.isHapticsEnabled,
                    onOptionsClick = onOptionsClick
                )
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
}

@Composable
fun PortraitLayout(
    score: Int,
    moves: Int,
    viewModel: JewelindaViewModel,
    particleViewModel: ParticleViewModel,
    isHapticsEnabled: Boolean,
    onOptionsClick: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val topOffset10 = maxHeight * 0.1f
        val topOffset15 = maxHeight * 0.15f

        // Info: Score and Target
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp)
                .offset(y = topOffset10)
        ) {
            Text(
                text = "Score: $score",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Target: ${JewelindaViewModel.TARGET_SCORE}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        // Info: Moves
        Text(
            text = "Moves: $moves",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp)
                .offset(y = topOffset15),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = if (moves <= 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )

        // Buttons on the right
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 16.dp)
                .offset(y = topOffset10),
            horizontalAlignment = Alignment.End
        ) {
            Button(
                onClick = { viewModel.newGame() },
                modifier = Modifier.height(40.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
            ) {
                Text("New Game", fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onOptionsClick,
                modifier = Modifier.height(40.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
            ) {
                Text("Options", fontSize = 13.sp)
            }
        }

        // Board explicitly centered in full space
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            GameGrid(
                viewModel = viewModel,
                particleViewModel = particleViewModel,
                isHapticsEnabled = isHapticsEnabled
            )
        }
    }
}

@Composable
fun LandscapeLayout(
    score: Int,
    moves: Int,
    viewModel: JewelindaViewModel,
    particleViewModel: ParticleViewModel,
    isHapticsEnabled: Boolean,
    onOptionsClick: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val topOffset10 = maxHeight * 0.1f

        Row(modifier = Modifier.fillMaxSize()) {
            // Left Column: Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 16.dp, top = topOffset10),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Score: $score",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Target: ${JewelindaViewModel.TARGET_SCORE}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Moves: $moves",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (moves <= 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }

            // Middle Column: Board
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                GameGrid(
                    viewModel = viewModel,
                    particleViewModel = particleViewModel,
                    isHapticsEnabled = isHapticsEnabled
                )
            }

            // Right Column: Buttons
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(end = 16.dp, top = topOffset10),
                horizontalAlignment = Alignment.End
            ) {
                Button(
                    onClick = { viewModel.newGame() },
                    modifier = Modifier.height(40.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Text("New Game", fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onOptionsClick,
                    modifier = Modifier.height(40.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Text("Options", fontSize = 13.sp)
                }
            }
        }
    }
}
