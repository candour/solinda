package com.example.solinda.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object GameTypeIcons {
    val Klondike: ImageVector by lazy {
        ImageVector.Builder(
            name = "Klondike",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.EvenOdd
        ) {
            // Card outline
            moveTo(4f, 2f)
            arcTo(2f, 2f, 0f, false, false, 2f, 4f)
            verticalLineTo(20f)
            arcTo(2f, 2f, 0f, false, false, 4f, 22f)
            horizontalLineTo(20f)
            arcTo(2f, 2f, 0f, false, false, 22f, 20f)
            verticalLineTo(4f)
            arcTo(2f, 2f, 0f, false, false, 20f, 2f)
            horizontalLineTo(4f)
            close()
            // S character (simplified)
            moveTo(15f, 8f)
            horizontalLineTo(10f)
            verticalLineTo(11f)
            horizontalLineTo(14f)
            verticalLineTo(16f)
            horizontalLineTo(9f)
            verticalLineTo(14f)
            horizontalLineTo(13f)
            verticalLineTo(13f)
            horizontalLineTo(9f)
            verticalLineTo(8f)
            arcTo(1f, 1f, 0f, false, true, 10f, 7f)
            horizontalLineTo(14f)
            arcTo(1f, 1f, 0f, false, true, 15f, 8f)
            close()
        }.build()
    }

    val FreeCell: ImageVector by lazy {
        ImageVector.Builder(
            name = "FreeCell",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.EvenOdd
        ) {
            // Card outline
            moveTo(4f, 2f)
            arcTo(2f, 2f, 0f, false, false, 2f, 4f)
            verticalLineTo(20f)
            arcTo(2f, 2f, 0f, false, false, 4f, 22f)
            horizontalLineTo(20f)
            arcTo(2f, 2f, 0f, false, false, 22f, 20f)
            verticalLineTo(4f)
            arcTo(2f, 2f, 0f, false, false, 20f, 2f)
            horizontalLineTo(4f)
            close()
            // F character
            moveTo(9f, 7f)
            horizontalLineTo(15f)
            verticalLineTo(9f)
            horizontalLineTo(11f)
            verticalLineTo(11f)
            horizontalLineTo(14f)
            verticalLineTo(13f)
            horizontalLineTo(11f)
            verticalLineTo(17f)
            horizontalLineTo(9f)
            close()
        }.build()
    }

    val Jewelinda: ImageVector by lazy {
        ImageVector.Builder(
            name = "Jewelinda",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.EvenOdd
        ) {
            moveTo(12f, 2f)
            lineTo(22f, 12f)
            lineTo(12f, 22f)
            lineTo(2f, 12f)
            close()
        }.build()
    }

    val Compass: ImageVector by lazy {
        ImageVector.Builder(
            name = "Compass",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.EvenOdd
        ) {
            moveTo(12f, 2f)
            arcTo(10f, 10f, 0f, true, false, 22f, 12f)
            arcTo(10f, 10f, 0f, false, false, 12f, 2f)
            close()
            moveTo(12f, 20f)
            arcTo(8f, 8f, 0f, true, true, 20f, 12f)
            arcTo(8f, 8f, 0f, false, true, 12f, 20f)
            close()
            moveTo(15f, 9f)
            lineTo(13f, 13f)
            lineTo(9f, 15f)
            lineTo(11f, 11f)
            close()
        }.build()
    }

    val Calculator: ImageVector by lazy {
        ImageVector.Builder(
            name = "Calculator",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.EvenOdd
        ) {
            moveTo(19f, 3f)
            horizontalLineTo(5f)
            arcTo(2f, 2f, 0f, false, false, 3f, 5f)
            verticalLineTo(19f)
            arcTo(2f, 2f, 0f, false, false, 5f, 21f)
            horizontalLineTo(19f)
            arcTo(2f, 2f, 0f, false, false, 21f, 19f)
            verticalLineTo(5f)
            arcTo(2f, 2f, 0f, false, false, 19f, 3f)
            close()
            moveTo(17f, 9f)
            horizontalLineTo(7f)
            verticalLineTo(7f)
            horizontalLineTo(17f)
            close()
            moveTo(9f, 13f)
            horizontalLineTo(7f)
            verticalLineTo(11f)
            horizontalLineTo(9f)
            close()
            moveTo(13f, 13f)
            horizontalLineTo(11f)
            verticalLineTo(11f)
            horizontalLineTo(13f)
            close()
            moveTo(17f, 13f)
            horizontalLineTo(15f)
            verticalLineTo(11f)
            horizontalLineTo(17f)
            close()
            moveTo(9f, 17f)
            horizontalLineTo(7f)
            verticalLineTo(15f)
            horizontalLineTo(9f)
            close()
            moveTo(13f, 17f)
            horizontalLineTo(11f)
            verticalLineTo(15f)
            horizontalLineTo(13f)
            close()
            moveTo(17f, 17f)
            horizontalLineTo(15f)
            verticalLineTo(15f)
            horizontalLineTo(17f)
            close()
        }.build()
    }
}
