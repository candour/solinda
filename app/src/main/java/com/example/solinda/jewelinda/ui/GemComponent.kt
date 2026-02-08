package com.example.solinda.jewelinda.ui

import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.solinda.jewelinda.Gem
import com.example.solinda.jewelinda.GemType
import kotlin.math.roundToInt

@Composable
fun GemComponent(gem: Gem, size: Dp, modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val sizePx = with(density) { size.toPx() }

    val targetOffset = IntOffset(
        x = (gem.posX * sizePx).roundToInt(),
        y = (gem.posY * sizePx).roundToInt()
    )

    val animatedOffset by animateIntOffsetAsState(
        targetValue = targetOffset,
        label = "gemOffset"
    )

    Box(
        modifier = modifier
            .size(size)
            .offset { animatedOffset }
            .padding(4.dp)
            .background(color = getGemColor(gem.type), shape = CircleShape)
    )
}

private fun getGemColor(type: GemType): Color {
    return when (type) {
        GemType.RED -> Color.Red
        GemType.BLUE -> Color.Blue
        GemType.GREEN -> Color.Green
        GemType.YELLOW -> Color.Yellow
        GemType.PURPLE -> Color.Magenta
        GemType.ORANGE -> Color(0xFFFFA500)
    }
}
