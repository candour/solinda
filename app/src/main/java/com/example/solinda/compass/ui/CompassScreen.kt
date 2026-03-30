package com.example.solinda.compass.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.solinda.compass.CompassViewModel
import kotlin.math.*

@Composable
fun CompassScreen(
    viewModel: CompassViewModel,
    onOptionsClick: () -> Unit
) {
    val azimuth by viewModel.azimuth.collectAsState()

    // Smoothly animate the azimuth rotation to avoid jittery movements
    val animatedAzimuth = remember { Animatable(azimuth) }

    LaunchedEffect(azimuth) {
        var delta = azimuth - animatedAzimuth.value
        while (delta > 180f) delta -= 360f
        while (delta < -180f) delta += 360f
        animatedAzimuth.animateTo(
            targetValue = animatedAzimuth.value + delta,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    DisposableEffect(viewModel) {
        viewModel.startListening()
        onDispose {
            viewModel.stopListening()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isPortrait = maxWidth < maxHeight

            // Options Button
            Button(
                onClick = onOptionsClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .then(
                        if (isPortrait) Modifier.offset(y = maxHeight * 0.1f) else Modifier
                    )
            ) {
                Text("Options")
            }

            // Compass Container
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CompassDial(rotation = -animatedAzimuth.value)

                // Current heading text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 64.dp)
                ) {
                    Text(
                        text = "${azimuth.roundToInt()}°",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = getCardinalDirection(azimuth),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }
}

@Composable
fun CompassDial(rotation: Float) {
    val textMeasurer = rememberTextMeasurer()
    val onBackground = MaterialTheme.colorScheme.onBackground

    Canvas(modifier = Modifier.size(300.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2
        val strokeWidth = 4.dp.toPx()

        // Outer circle
        drawCircle(
            color = Color.DarkGray,
            radius = radius,
            center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth)
        )

        // Rotate the entire dial based on the device heading
        rotate(rotation, center) {
            // Cardinal Marks
            val directions = listOf("N", "E", "S", "W")
            directions.forEachIndexed { index, label ->
                val angle = index * 90f
                val angleRad = Math.toRadians(angle.toDouble() - 90).toFloat()

                // Draw a tick mark
                val tickStart = Offset(
                    center.x + (radius - 12.dp.toPx()) * cos(angleRad),
                    center.y + (radius - 12.dp.toPx()) * sin(angleRad)
                )
                val tickEnd = Offset(
                    center.x + radius * cos(angleRad),
                    center.y + radius * sin(angleRad)
                )

                drawLine(
                    color = onBackground,
                    start = tickStart,
                    end = tickEnd,
                    strokeWidth = strokeWidth
                )

                // Draw Label
                val textLayoutResult = textMeasurer.measure(
                    text = label,
                    style = TextStyle(
                        color = if (label == "N") Color.Red else onBackground,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                val textOffset = Offset(
                    center.x + (radius - 35.dp.toPx()) * cos(angleRad) - textLayoutResult.size.width / 2,
                    center.y + (radius - 35.dp.toPx()) * sin(angleRad) - textLayoutResult.size.height / 2
                )

                // Counter-rotate text so it stays upright
                rotate(-rotation - angle, Offset(textOffset.x + textLayoutResult.size.width / 2, textOffset.y + textLayoutResult.size.height / 2)) {
                    drawText(textLayoutResult, topLeft = textOffset)
                }
            }

            // Smaller ticks every 10 degrees
            for (i in 0 until 360 step 10) {
                if (i % 90 == 0) continue
                val angleRad = Math.toRadians(i.toDouble() - 90).toFloat()
                val tickStart = Offset(
                    center.x + (radius - 5.dp.toPx()) * cos(angleRad),
                    center.y + (radius - 5.dp.toPx()) * sin(angleRad)
                )
                val tickEnd = Offset(
                    center.x + radius * cos(angleRad),
                    center.y + radius * sin(angleRad)
                )
                drawLine(
                    color = Color.Gray,
                    start = tickStart,
                    end = tickEnd,
                    strokeWidth = 2.dp.toPx()
                )
            }

            // Compass Needle
            // North (Red)
            drawLine(
                color = Color.Red,
                start = center,
                end = Offset(center.x, center.y - radius + 20.dp.toPx()),
                strokeWidth = strokeWidth * 2,
                cap = StrokeCap.Round
            )
            // South (Black)
            drawLine(
                color = Color.Black,
                start = center,
                end = Offset(center.x, center.y + radius - 20.dp.toPx()),
                strokeWidth = strokeWidth * 2,
                cap = StrokeCap.Round
            )

            // Central pin
            drawCircle(Color.DarkGray, radius = 8.dp.toPx(), center = center)
            drawCircle(Color.LightGray, radius = 4.dp.toPx(), center = center)
        }

        // Static indicator at the top
        drawLine(
            color = Color.Magenta,
            start = Offset(center.x, center.y - radius - 10.dp.toPx()),
            end = Offset(center.x, center.y - radius + 10.dp.toPx()),
            strokeWidth = strokeWidth
        )
    }
}

private fun getCardinalDirection(azimuth: Float): String {
    return when (azimuth) {
        in 337.5..360.0 -> "N"
        in 0.0..22.5 -> "N"
        in 22.5..67.5 -> "NE"
        in 67.5..112.5 -> "E"
        in 112.5..157.5 -> "SE"
        in 157.5..202.5 -> "S"
        in 202.5..247.5 -> "SW"
        in 247.5..292.5 -> "W"
        in 292.5..337.5 -> "NW"
        else -> "N"
    }
}
