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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import com.example.solinda.GameViewModel
import com.example.solinda.jewelinda.GameBoard
import com.example.solinda.jewelinda.GemType
import com.example.solinda.jewelinda.JewelindaEvent
import com.example.solinda.jewelinda.JewelindaViewModel
import com.example.solinda.jewelinda.LevelType
import com.example.solinda.jewelinda.ParticleViewModel
import com.example.solinda.jewelinda.getGemColor

@Composable
fun ObjectiveBar(
    objectives: Map<GemType, Int>,
    levelType: LevelType,
    board: GameBoard
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Color objectives
        objectives.forEach { (type, count) ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                Image(
                    imageVector = getGemIcon(type),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(getGemColor(type))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "x$count",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (count <= 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Frost objective
        if (levelType == LevelType.FROST_CLEARANCE || levelType == LevelType.HYBRID) {
            var frostCount = 0
            for (y in 0 until GameBoard.HEIGHT) {
                for (x in 0 until GameBoard.WIDTH) {
                    if (board.getFrostLevel(x, y) > 0) frostCount++
                }
            }

            if (objectives.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.4f))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "x$frostCount",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (frostCount <= 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun JewelindaScreen(
    viewModel: JewelindaViewModel,
    gameViewModel: GameViewModel,
    onOptionsClick: () -> Unit
) {
    val board by viewModel.board.collectAsState()
    val score by viewModel.score.collectAsState()
    val moves by viewModel.movesRemaining.collectAsState()
    val levelType by viewModel.levelType.collectAsState()
    val objectives by viewModel.objectives.collectAsState()
    val particleViewModel: ParticleViewModel = viewModel()
    val view = LocalView.current
    val density = LocalDensity.current

    var shakeOffset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is JewelindaEvent.MatchPerformed -> {
                    if (gameViewModel.isHapticsEnabled) {
                        if (event.isFrostCleared) {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        }
                        val hapticType = when {
                            event.size >= 5 -> HapticFeedbackConstants.LONG_PRESS
                            event.size >= 4 -> HapticFeedbackConstants.KEYBOARD_TAP
                            else -> HapticFeedbackConstants.VIRTUAL_KEY
                        }
                        view.performHapticFeedback(hapticType)
                    }
                }
                is JewelindaEvent.BombExploded -> {
                    if (gameViewModel.isHapticsEnabled) {
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    }
                    // Screen shake for 300ms
                    val startTime = System.currentTimeMillis()
                    while (System.currentTimeMillis() - startTime < 300) {
                        val magnitude = 6.dp
                        shakeOffset = with(density) {
                            Offset(
                                ((Math.random().toFloat() * 2 - 1) * magnitude.toPx()),
                                ((Math.random().toFloat() * 2 - 1) * magnitude.toPx())
                            )
                        }
                        delay(20)
                    }
                    shakeOffset = Offset.Zero
                }
                else -> {}
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
                    levelType = levelType,
                    objectives = objectives,
                    board = board,
                    viewModel = viewModel,
                    particleViewModel = particleViewModel,
                    isHapticsEnabled = gameViewModel.isHapticsEnabled,
                    shakeOffset = shakeOffset,
                    onOptionsClick = onOptionsClick
                )
            } else {
                PortraitLayout(
                    score = score,
                    moves = moves,
                    levelType = levelType,
                    objectives = objectives,
                    board = board,
                    viewModel = viewModel,
                    particleViewModel = particleViewModel,
                    isHapticsEnabled = gameViewModel.isHapticsEnabled,
                    shakeOffset = shakeOffset,
                    onOptionsClick = onOptionsClick
                )
            }

            if (moves <= 0) {
                GameOverOverlay(
                    score = score,
                    isWin = viewModel.checkWinCondition(),
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
    levelType: LevelType,
    objectives: Map<GemType, Int>,
    board: GameBoard,
    viewModel: JewelindaViewModel,
    particleViewModel: ParticleViewModel,
    isHapticsEnabled: Boolean,
    shakeOffset: Offset,
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

        // Objective Bar between info and board
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .offset(y = topOffset10 + 60.dp) // Below score
        ) {
            ObjectiveBar(objectives = objectives, levelType = levelType, board = board)
        }

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(shakeOffset.x.roundToInt(), shakeOffset.y.roundToInt()) },
            contentAlignment = Alignment.Center
        ) {
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
    levelType: LevelType,
    objectives: Map<GemType, Int>,
    board: GameBoard,
    viewModel: JewelindaViewModel,
    particleViewModel: ParticleViewModel,
    isHapticsEnabled: Boolean,
    shakeOffset: Offset,
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

                Spacer(modifier = Modifier.height(16.dp))
                ObjectiveBar(objectives = objectives, levelType = levelType, board = board)
            }

            // Middle Column: Board
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .offset { IntOffset(shakeOffset.x.roundToInt(), shakeOffset.y.roundToInt()) },
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

@Composable
fun ParticleOverlay(engine: ParticleEngine) {
    // 4. The Game Loop
    // Updates physics every frame (16ms)
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { 
                engine.update(1f) // 1 step per frame
            }
        }
    }

    // 5. The Rendering Layer
    // Skips recomposition, just redraws the canvas
    Canvas(modifier = Modifier.fillMaxSize()) {
        engine.particles.forEach { p ->
            if (p.alpha > 0f) {
                drawCircle(
                    color = p.color.copy(alpha = p.alpha),
                    radius = p.size,
                    center = Offset(p.x, p.y)
                )
            }
        }
    }
}

