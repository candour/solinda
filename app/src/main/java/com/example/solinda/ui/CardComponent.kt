package com.example.solinda.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.solinda.Card
import com.example.solinda.R

@Composable
fun CardComponent(
    card: Card,
    modifier: Modifier = Modifier,
    isDimmed: Boolean = false
) {
    val context = LocalContext.current
    val resourceId = if (card.faceUp) {
        context.resources.getIdentifier(card.imageName, "drawable", context.packageName)
    } else {
        context.resources.getIdentifier("back", "drawable", context.packageName)
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp)),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        if (resourceId != 0) {
            val colorFilter = if (isDimmed) {
                ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0.3f) })
            } else null

            Image(
                painter = painterResource(id = resourceId),
                contentDescription = if (card.faceUp) "${card.rank} of ${card.suit}" else "Card Back",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
                colorFilter = colorFilter,
                alpha = if (isDimmed) 0.6f else 1f
            )
        } else {
            // Fallback if resource not found
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = if (card.faceUp) Color.White else Color.DarkGray
            ) {}
        }
    }
}
