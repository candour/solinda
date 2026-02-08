package com.example.solinda.jewelinda.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.example.solinda.jewelinda.ParticleViewModel

@Composable
fun ParticleLayer(viewModel: ParticleViewModel, gemSize: Dp) {
    val particles = viewModel.particles
    val density = LocalDensity.current
    // User asked for 1/10th of gem size. This is the diameter. So radius is gemSize / 20.
    val baseParticleSize = with(density) { (gemSize / 20f).toPx() }

    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos {
                viewModel.updateParticles()
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { p ->
            drawCircle(
                color = p.color,
                radius = baseParticleSize * p.scale,
                center = Offset(p.x, p.y),
                alpha = p.alpha
            )
        }
    }
}
