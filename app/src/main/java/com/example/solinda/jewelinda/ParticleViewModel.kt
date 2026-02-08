package com.example.solinda.jewelinda

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlin.random.Random

class ParticleViewModel : ViewModel() {
    private val _particles = mutableStateListOf<Particle>()
    val particles: List<Particle> = _particles

    fun spawnBurst(x: Float, y: Float, color: Color) {
        val count = (20..30).random()
        repeat(count) {
            _particles.add(
                Particle(
                    x = x,
                    y = y,
                    velocityX = (Random.nextFloat() - 0.5f) * 15f,
                    velocityY = (Random.nextFloat() - 0.5f) * 15f - 5f, // Slight upward bias
                    alpha = 1.0f,
                    scale = 1.0f,
                    color = color
                )
            )
        }
    }

    fun updateParticles() {
        if (_particles.isEmpty()) return

        val iterator = _particles.listIterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            val newAlpha = p.alpha - DECAY
            if (newAlpha <= 0) {
                iterator.remove()
            } else {
                // Shrink scale to 0.0f as alpha approaches 0
                // We'll have it start shrinking when alpha is below 0.5
                val newScale = (newAlpha / 0.5f).coerceAtMost(1.0f)

                iterator.set(p.copy(
                    x = p.x + p.velocityX,
                    y = p.y + p.velocityY,
                    velocityY = p.velocityY + GRAVITY,
                    alpha = newAlpha,
                    scale = newScale
                ))
            }
        }
    }

    companion object {
        const val GRAVITY = 0.8f
        const val DECAY = 0.02f
    }
}
