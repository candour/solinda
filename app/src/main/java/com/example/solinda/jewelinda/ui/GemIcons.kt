package com.example.solinda.jewelinda.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object GemIcons {
    val Diamond: ImageVector by lazy {
        ImageVector.Builder(
            name = "Diamond",
            defaultWidth = 100.dp,
            defaultHeight = 100.dp,
            viewportWidth = 100f,
            viewportHeight = 100f
        ).path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.EvenOdd
        ) {
            moveTo(50f, 5f)
            lineTo(95f, 50f)
            lineTo(50f, 95f)
            lineTo(5f, 50f)
            close()
        }.build()
    }

    val Square: ImageVector by lazy {
        ImageVector.Builder(
            name = "Square",
            defaultWidth = 100.dp,
            defaultHeight = 100.dp,
            viewportWidth = 100f,
            viewportHeight = 100f
        ).path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.EvenOdd
        ) {
            moveTo(10f, 10f)
            lineTo(90f, 10f)
            lineTo(90f, 90f)
            lineTo(10f, 90f)
            close()
        }.build()
    }

    val Circle: ImageVector by lazy {
        ImageVector.Builder(
            name = "Circle",
            defaultWidth = 100.dp,
            defaultHeight = 100.dp,
            viewportWidth = 100f,
            viewportHeight = 100f
        ).path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.EvenOdd
        ) {
            moveTo(50f, 5f)
            arcTo(45f, 45f, 0f, isMoreThanHalf = true, isPositiveArc = true, x1 = 50f, y1 = 95f)
            arcTo(45f, 45f, 0f, isMoreThanHalf = true, isPositiveArc = true, x1 = 50f, y1 = 5f)
            close()
        }.build()
    }

    val Hexagon: ImageVector by lazy {
        ImageVector.Builder(
            name = "Hexagon",
            defaultWidth = 100.dp,
            defaultHeight = 100.dp,
            viewportWidth = 100f,
            viewportHeight = 100f
        ).path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.EvenOdd
        ) {
            moveTo(50f, 5f)
            lineTo(90f, 27.5f)
            lineTo(90f, 72.5f)
            lineTo(50f, 95f)
            lineTo(10f, 72.5f)
            lineTo(10f, 27.5f)
            close()
        }.build()
    }

    val Triangle: ImageVector by lazy {
        ImageVector.Builder(
            name = "Triangle",
            defaultWidth = 100.dp,
            defaultHeight = 100.dp,
            viewportWidth = 100f,
            viewportHeight = 100f
        ).path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.EvenOdd
        ) {
            moveTo(50f, 10f)
            lineTo(95f, 90f)
            lineTo(5f, 90f)
            close()
        }.build()
    }

    val Star: ImageVector by lazy {
        ImageVector.Builder(
            name = "Star",
            defaultWidth = 100.dp,
            defaultHeight = 100.dp,
            viewportWidth = 100f,
            viewportHeight = 100f
        ).path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.EvenOdd
        ) {
            // Standard 5-pointed star coordinates roughly centered in 100x100
            moveTo(50f, 5f)     // Top point
            lineTo(61f, 39f)    // Inner top right
            lineTo(98f, 39f)    // Outer right
            lineTo(68f, 60f)    // Inner bottom right
            lineTo(79f, 95f)    // Outer bottom right
            lineTo(50f, 73f)    // Bottom inner
            lineTo(21f, 95f)    // Outer bottom left
            lineTo(32f, 60f)    // Inner bottom left
            lineTo(2f, 39f)     // Outer left
            lineTo(39f, 39f)    // Inner top left
            close()
        }.build()
    }

    val Glint: ImageVector by lazy {
        ImageVector.Builder(
            name = "Glint",
            defaultWidth = 100.dp,
            defaultHeight = 100.dp,
            viewportWidth = 100f,
            viewportHeight = 100f
        ).path(
            fill = SolidColor(Color.White),
            pathFillType = PathFillType.NonZero
        ) {
            // A semi-oval highlight glint in the top-left area
            moveTo(30f, 20f)
            curveTo(40f, 15f, 60f, 15f, 70f, 20f)
            curveTo(65f, 30f, 35f, 30f, 30f, 20f)
            close()
        }.build()
    }

    val Flare: ImageVector by lazy {
        ImageVector.Builder(
            name = "Flare",
            defaultWidth = 100.dp,
            defaultHeight = 100.dp,
            viewportWidth = 100f,
            viewportHeight = 100f
        ).path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.EvenOdd
        ) {
            // A simple 8-pointed flare/sparkle
            moveTo(50f, 0f)
            lineTo(55f, 45f)
            lineTo(100f, 50f)
            lineTo(55f, 55f)
            lineTo(50f, 100f)
            lineTo(45f, 55f)
            lineTo(0f, 50f)
            lineTo(45f, 45f)
            close()

            moveTo(50f, 25f)
            lineTo(58f, 42f)
            lineTo(75f, 50f)
            lineTo(58f, 58f)
            lineTo(50f, 75f)
            lineTo(42f, 58f)
            lineTo(25f, 50f)
            lineTo(42f, 42f)
            close()
        }.build()
    }
}
