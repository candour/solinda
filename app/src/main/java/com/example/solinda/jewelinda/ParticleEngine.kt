import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.runtime.mutableStateListOf
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
    fun reset(startX: Float, startY: Float, color: Color) {
        x = startX
        y = startY
        // Random velocity (Explosion effect)
        vx = Random.nextFloat() * 10f - 5f 
        vy = Random.nextFloat() * 10f - 15f // Upward bias
        alpha = 1f
        this.color = color
        size = Random.nextFloat() * 15f + 5f
    }
}

class ParticleEngine {
    // 2. The Object Pool
    // Reuse these objects! Never delete them, just hide them.
    private val maxParticles = 150 
    val particles = mutableStateListOf<FastParticle>().apply {
        repeat(maxParticles) { add(FastParticle().apply { alpha = 0f }) }
    }

    fun spawnBurst(x: Float, y: Float, color: Color) {
        // Find 10-15 "dead" particles (alpha <= 0) and revive them
        var spawned = 0
        for (p in particles) {
            if (p.alpha <= 0f) {
                p.reset(x, y, color)
                spawned++
            }
            if (spawned >= 12) break // Hard limit per burst
        }
    }

    fun update(dt: Float) {
        // 3. The Physics Loop (No allocations here!)
        val gravity = 0.5f
        val decay = 0.02f
        
        particles.forEach { p ->
            if (p.alpha > 0f) {
                p.x += p.vx
                p.y += p.vy
                p.vy += gravity // Gravity
                p.alpha -= decay // Fade out
                if (p.alpha < 0f) p.alpha = 0f
            }
        }
    }
}

