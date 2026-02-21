package com.example.solinda.jewelinda

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

// 1. Mutable Data Class (Vital for performance)
// We use vars so we don't allocate new objects during the physics loop
class FastParticle(
    var x: Float = 0f,
    var y: Float = 0f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var alpha: Float = 1f,
    var color: Color = Color.White,
    var size: Float = 10f
) {
    fun reset(startX: Float, startY: Float, color: Color, size: Float) {
        x = startX
        y = startY
        // Random velocity (Explosion effect)
        vx = Random.nextFloat() * 10f - 5f 
        vy = Random.nextFloat() * 10f - 15f // Upward bias
        alpha = 1f
        this.color = color
        this.size = size
    }
}

class ParticleEngine {
    // 2. The Object Pool
    // Reuse these objects! Never delete them, just hide them.
    private val maxParticles = 150 
    val particles = mutableStateListOf<FastParticle>().apply {
        repeat(maxParticles) { add(FastParticle().apply { alpha = 0f }) }
    }

    // Signals if any particles are active to avoid unnecessary physics loops
    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

    // Force redraw without full recomposition
    var tick by mutableLongStateOf(0L)
        private set

    fun spawnBurst(x: Float, y: Float, color: Color, gemSizePx: Float) {
        // Find 12 "dead" particles (alpha <= 0) and revive them
        var spawned = 0
        for (p in particles) {
            if (p.alpha <= 0f) {
                val size = (gemSizePx * 0.1f) * Random.nextFloat()
                p.reset(x, y, color, size)
                spawned++
            }
            if (spawned >= 12) break // Hard limit per burst
        }
        if (spawned > 0) {
            _isActive.value = true
        }
    }

    fun update(dt: Float) {
        // 3. The Physics Loop (No allocations here!)
        val gravity = 0.5f
        val decay = 0.02f
        
        var hasActiveParticles = false
        particles.forEach { p ->
            if (p.alpha > 0f) {
                p.x += p.vx
                p.y += p.vy
                p.vy += gravity // Gravity
                p.alpha -= decay // Fade out
                if (p.alpha < 0f) p.alpha = 0f
                else hasActiveParticles = true
            }
        }

        if (hasActiveParticles) {
            tick++
        } else {
            _isActive.value = false
        }
    }
}
