package com.example.solinda.jewelinda.ui

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.min
import com.example.solinda.jewelinda.Direction
import com.example.solinda.jewelinda.GameBoard
import com.example.solinda.jewelinda.JewelindaEvent
import com.example.solinda.jewelinda.JewelindaViewModel
import com.example.solinda.jewelinda.ParticleViewModel
import com.example.solinda.jewelinda.getGemColor
import kotlin.math.abs

@Composable
fun GameGrid(viewModel: JewelindaViewModel, particleViewModel: ParticleViewModel) {
    val board by viewModel.board.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val density = LocalDensity.current

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val gridSize = min(maxWidth, maxHeight)
        val gemSize = gridSize / 8

        LaunchedEffect(viewModel.events) {
            viewModel.events.collect { event ->
                if (event is JewelindaEvent.GemCleared) {
                    val gemSizePx = with(density) { gemSize.toPx() }
                    val centerX = (event.x + 0.5f) * gemSizePx
                    val centerY = (event.y + 0.5f) * gemSizePx
                    particleViewModel.spawnBurst(centerX, centerY, getGemColor(event.type))
                }
            }
        }

        var sourceGemCoords by remember { mutableStateOf<Pair<Int, Int>?>(null) }
        var totalDrag by remember { mutableStateOf(Offset.Zero) }
        var dragTriggered by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .size(gridSize)
                .align(Alignment.Center)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            if (!isProcessing) {
                                val gemSizePx = size.width.toFloat() / 8f
                                val x = (offset.x / gemSizePx).toInt()
                                val y = (offset.y / gemSizePx).toInt()
                                if (x in 0 until GameBoard.WIDTH && y in 0 until GameBoard.HEIGHT) {
                                    sourceGemCoords = Pair(x, y)
                                    totalDrag = Offset.Zero
                                    dragTriggered = false
                                }
                            }
                        },
                        onDrag = { _, dragAmount ->
                            if (!isProcessing && sourceGemCoords != null && !dragTriggered) {
                                totalDrag += dragAmount
                                val threshold = 50f // pixels
                                if (totalDrag.getDistance() > threshold) {
                                    val direction = when {
                                        abs(totalDrag.x) > abs(totalDrag.y) -> {
                                            if (totalDrag.x > 0) Direction.EAST else Direction.WEST
                                        }
                                        else -> {
                                            if (totalDrag.y > 0) Direction.SOUTH else Direction.NORTH
                                        }
                                    }
                                    viewModel.onSwipe(sourceGemCoords!!.first, sourceGemCoords!!.second, direction)
                                    dragTriggered = true
                                }
                            }
                        },
                        onDragEnd = {
                            sourceGemCoords = null
                        },
                        onDragCancel = {
                            sourceGemCoords = null
                        }
                    )
                }
        ) {
            for (y in 0 until GameBoard.HEIGHT) {
                for (x in 0 until GameBoard.WIDTH) {
                    board.getGem(x, y)?.let { gem ->
                        GemComponent(gem = gem, size = gemSize)
                    }
                }
            }
            ParticleLayer(viewModel = particleViewModel, gemSize = gemSize)
        }
    }
}
