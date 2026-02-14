package com.example.solinda.jewelinda.ui

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.example.solinda.jewelinda.Direction
import com.example.solinda.jewelinda.GameBoard
import com.example.solinda.jewelinda.JewelindaEvent
import com.example.solinda.jewelinda.JewelindaViewModel
import com.example.solinda.jewelinda.ParticleViewModel
import com.example.solinda.jewelinda.getGemColor
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun FrostTile(level: Int, size: Dp, x: Int, y: Int) {
    if (level <= 0) return
    val density = LocalDensity.current
    val sizePx = with(density) { size.toPx() }

    Box(
        modifier = Modifier
            .size(size)
            .offset {
                IntOffset((x * sizePx).roundToInt(), (y * sizePx).roundToInt())
            }
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.3f))
    ) {
        if (level >= 2) {
            Image(
                imageVector = GemIcons.CrackedIce,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.5f))
            )
        }
    }
}

@Composable
fun GameGrid(
    viewModel: JewelindaViewModel,
    particleViewModel: ParticleViewModel,
    isHapticsEnabled: Boolean
) {
    val board by viewModel.board.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val isGravityEnabled by viewModel.isGravityEnabled.collectAsState()
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
        var sourceGemId by remember { mutableStateOf<UUID?>(null) }
        var targetGemId by remember { mutableStateOf<UUID?>(null) }
        val dragOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
        var dragTriggered by remember { mutableStateOf(false) }

        val scope = rememberCoroutineScope()
        val view = LocalView.current

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
                                    if (board.getFrostLevel(x, y) == 0) {
                                        sourceGemCoords = Pair(x, y)
                                        sourceGemId = board.getGem(x, y)?.id
                                        targetGemId = null
                                        dragTriggered = false
                                        scope.launch { dragOffset.snapTo(Offset.Zero) }
                                    }
                                }
                            }
                        },
                        onDrag = { _, dragAmount ->
                            val currentSourceCoords = sourceGemCoords
                            if (!isProcessing && currentSourceCoords != null && !dragTriggered) {
                                val gemSizePx = size.width.toFloat() / 8f
                                val currentDrag = dragOffset.value + dragAmount
                                val clampedDrag = Offset(
                                    x = currentDrag.x.coerceIn(-gemSizePx, gemSizePx),
                                    y = currentDrag.y.coerceIn(-gemSizePx, gemSizePx)
                                )
                                scope.launch { dragOffset.snapTo(clampedDrag) }

                                val direction = when {
                                    abs(clampedDrag.x) > abs(clampedDrag.y) -> {
                                        if (clampedDrag.x > 0) Direction.EAST else Direction.WEST
                                    }
                                    else -> {
                                        if (clampedDrag.y > 0) Direction.SOUTH else Direction.NORTH
                                    }
                                }

                                val targetX = when (direction) {
                                    Direction.EAST -> currentSourceCoords.first + 1
                                    Direction.WEST -> currentSourceCoords.first - 1
                                    else -> currentSourceCoords.first
                                }
                                val targetY = when (direction) {
                                    Direction.SOUTH -> currentSourceCoords.second + 1
                                    Direction.NORTH -> currentSourceCoords.second - 1
                                    else -> currentSourceCoords.second
                                }

                                val isTargetInBounds = targetX in 0 until GameBoard.WIDTH && targetY in 0 until GameBoard.HEIGHT
                                val isTargetFrosted = isTargetInBounds && board.getFrostLevel(targetX, targetY) > 0

                                if (isTargetFrosted || !isTargetInBounds) {
                                    targetGemId = null
                                } else {
                                    targetGemId = board.getGem(targetX, targetY)?.id
                                }

                                val threshold = 50f // pixels
                                if (clampedDrag.getDistance() > threshold) {
                                    if (isTargetFrosted || !isTargetInBounds) {
                                        // Block the swipe
                                        dragTriggered = true
                                        scope.launch {
                                            dragOffset.animateTo(Offset.Zero, SnappySpringOffset)
                                            sourceGemId = null
                                            targetGemId = null
                                        }
                                    } else {
                                        if (isHapticsEnabled) {
                                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                        }
                                        viewModel.onSwipe(currentSourceCoords.first, currentSourceCoords.second, direction)
                                        dragTriggered = true
                                        scope.launch {
                                            dragOffset.animateTo(Offset.Zero, SnappySpringOffset)
                                            sourceGemId = null
                                            targetGemId = null
                                        }
                                    }
                                }
                            }
                        },
                        onDragEnd = {
                            if (!dragTriggered) {
                                scope.launch {
                                    dragOffset.animateTo(Offset.Zero, SnappySpringOffset)
                                    sourceGemCoords = null
                                    sourceGemId = null
                                    targetGemId = null
                                }
                            } else {
                                sourceGemCoords = null
                            }
                        },
                        onDragCancel = {
                            if (!dragTriggered) {
                                scope.launch {
                                    dragOffset.animateTo(Offset.Zero, SnappySpringOffset)
                                    sourceGemCoords = null
                                    sourceGemId = null
                                    targetGemId = null
                                }
                            } else {
                                sourceGemCoords = null
                            }
                        }
                    )
                }
        ) {
            // Background Frost Layer
            for (y in 0 until GameBoard.HEIGHT) {
                for (x in 0 until GameBoard.WIDTH) {
                    val frostLevel = board.getFrostLevel(x, y)
                    if (frostLevel > 0) {
                        key("frost_$x-$y") {
                            FrostTile(level = frostLevel, size = gemSize, x = x, y = y)
                        }
                    }
                }
            }

            // Gems Layer
            for (y in 0 until GameBoard.HEIGHT) {
                for (x in 0 until GameBoard.WIDTH) {
                    board.getGem(x, y)?.let { gem ->
                        val offset = when (gem.id) {
                            sourceGemId -> dragOffset.value
                            targetGemId -> -dragOffset.value
                            else -> Offset.Zero
                        }
                        key(gem.id) {
                            GemComponent(
                                gem = gem,
                                size = gemSize,
                                isGravityEnabled = isGravityEnabled,
                                dragOffset = offset
                            )
                        }
                    }
                }
            }
            ParticleLayer(viewModel = particleViewModel, gemSize = gemSize)
        }
    }
}
