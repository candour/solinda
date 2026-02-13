package com.example.solinda.jewelinda.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.solinda.jewelinda.Gem
import com.example.solinda.jewelinda.GemType
import com.example.solinda.jewelinda.getGemColor
import kotlin.math.roundToInt

val SnappySpring = spring<IntOffset>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMedium
)

val SnappySpringOffset = spring<Offset>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMedium
)

fun getGemIcon(type: GemType): ImageVector {
    return when (type) {
        GemType.RED -> GemIcons.Diamond
        GemType.BLUE -> GemIcons.Square
        GemType.GREEN -> GemIcons.Circle
        GemType.YELLOW -> GemIcons.Hexagon
        GemType.PURPLE -> GemIcons.Triangle
        GemType.ORANGE -> GemIcons.Star
    }
}

@Composable
fun GemComponent(
    gem: Gem,
    size: Dp,
    isGravityEnabled: Boolean,
    dragOffset: Offset = Offset.Zero,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val sizePx = with(density) { size.toPx() }

    val targetOffset = IntOffset(
        x = (gem.posX * sizePx).roundToInt(),
        y = (gem.posY * sizePx).roundToInt()
    )

    var lastPosY by remember { mutableIntStateOf(gem.posY) }
    var willSquashOnFinish by remember { mutableStateOf(false) }
    var isSquashing by remember { mutableStateOf(false) }

    LaunchedEffect(gem.posY) {
        if (gem.posY > lastPosY && isGravityEnabled) {
            willSquashOnFinish = true
        }
        lastPosY = gem.posY
    }

    val scaleX by animateFloatAsState(
        targetValue = if (isSquashing) 1.2f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        finishedListener = { if (isSquashing) isSquashing = false },
        label = "scaleX"
    )
    val scaleY by animateFloatAsState(
        targetValue = if (isSquashing) 0.8f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scaleY"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "bombPulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "pulse"
    )

    val animatedOffset by animateIntOffsetAsState(
        targetValue = targetOffset,
        label = "gemOffset",
        animationSpec = SnappySpring,
        finishedListener = {
            if (willSquashOnFinish) {
                isSquashing = true
                willSquashOnFinish = false
            }
        }
    )

    Box(
        modifier = modifier
            .size(size)
            .offset {
                IntOffset(
                    x = animatedOffset.x + dragOffset.x.roundToInt(),
                    y = animatedOffset.y + dragOffset.y.roundToInt()
                )
            }
            .graphicsLayer {
                this.scaleX = scaleX * if (gem.isBomb) pulse else 1f
                this.scaleY = scaleY * if (gem.isBomb) pulse else 1f
                this.transformOrigin = TransformOrigin(0.5f, 1f)
            }
            .padding(4.dp)
    ) {
        val icon = getGemIcon(gem.type)

        if (gem.isBomb) {
            // Bomb Layer: Glow/Flare
            Image(
                imageVector = GemIcons.Flare,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                colorFilter = ColorFilter.tint(getGemColor(gem.type).copy(alpha = 0.6f))
            )
        }

        // Shadow Layer (Offset Darkened Version)
        Image(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize(if (gem.isBomb) 0.8f else 1f)
                .align(Alignment.Center)
                .offset(x = 2.dp, y = 2.dp),
            colorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.3f))
        )

        // Base Layer (Main Gem Color)
        Image(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize(if (gem.isBomb) 0.8f else 1f)
                .align(Alignment.Center),
            colorFilter = ColorFilter.tint(getGemColor(gem.type))
        )

        // Highlight Layer (Glint)
        Image(
            imageVector = GemIcons.Glint,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize(if (gem.isBomb) 0.8f else 1f)
                .align(Alignment.Center),
            colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.4f))
        )
    }
}
