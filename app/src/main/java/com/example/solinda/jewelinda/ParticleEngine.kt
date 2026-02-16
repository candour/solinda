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
