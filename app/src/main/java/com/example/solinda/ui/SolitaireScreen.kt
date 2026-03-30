package com.example.solinda.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.solinda.*
import kotlin.math.roundToInt

@Composable
fun SolitaireScreen(
    viewModel: GameViewModel,
    repository: GameRepository,
    onOptionsClick: () -> Unit
) {
    var screenWidth by remember { mutableStateOf(0f) }
    var screenHeight by remember { mutableStateOf(0f) }
    val density = LocalDensity.current

    val cardWidth = remember(screenWidth, screenHeight, viewModel.gameType, viewModel.leftMargin, viewModel.rightMargin, viewModel.leftMarginLandscape, viewModel.rightMarginLandscape) {
        if (screenWidth > 0) {
            val isLandscape = screenWidth > screenHeight
            val numPiles = viewModel.tableau.size.coerceAtLeast(7)
            val leftMarginPx = with(density) { (if (isLandscape) viewModel.leftMarginLandscape else viewModel.leftMargin).dp.toPx() }
            val rightMarginPx = with(density) { (if (isLandscape) viewModel.rightMarginLandscape else viewModel.rightMargin).dp.toPx() }
            val totalSpacing = (numPiles - 1) * with(density) { 8.dp.toPx() }
            (screenWidth - leftMarginPx - rightMarginPx - totalSpacing) / numPiles
        } else 0f
    }
    val cardHeight = remember(cardWidth) { cardWidth * 1.4f }

    var draggingStack by remember { mutableStateOf<List<Card>?>(null) }
    var draggingFromPile by remember { mutableStateOf<Pile?>(null) }
    var dragPosition by remember { mutableStateOf(Offset.Zero) }
    var dragStartOffset by remember { mutableStateOf(Offset.Zero) }

    val isLandscape = screenWidth > screenHeight
    val leftMargin = (if (isLandscape) viewModel.leftMarginLandscape else viewModel.leftMargin).dp
    val topMargin = if (isLandscape) 16.dp else 64.dp
    val spacing = 8.dp

    val cardWidthDp = with(density) { cardWidth.toDp() }
    val cardHeightDp = with(density) { cardHeight.toDp() }

    fun getPileRect(pile: Pile, index: Int): Rect {
        val x = with(density) {
            val startX = leftMargin.toPx()
            val pileSpacing = (cardWidth + spacing.toPx())
            when (pile.type) {
                PileType.STOCK -> startX
                PileType.WASTE -> startX + pileSpacing
                PileType.FOUNDATION -> {
                    if (viewModel.gameType == GameType.FREECELL) {
                        startX + (4 + index) * pileSpacing
                    } else {
                        val rMargin = (if (isLandscape) viewModel.rightMarginLandscape else viewModel.rightMargin).dp.toPx()
                        screenWidth - rMargin - (4 - index) * pileSpacing
                    }
                }
                PileType.TABLEAU -> startX + index * pileSpacing
                PileType.FREE_CELL -> startX + index * pileSpacing
            }
        }
        val y = with(density) {
            val topY = topMargin.toPx()
            if (pile.type == PileType.TABLEAU) {
                topY + cardHeight + with(density) { 24.dp.toPx() }
            } else {
                topY
            }
        }
        return Rect(x, y, x + cardWidth, y + cardHeight)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B6623))
            .onGloballyPositioned {
                screenWidth = it.size.width.toFloat()
                screenHeight = it.size.height.toFloat()
            }
            .pointerInput(cardWidth, cardHeight, viewModel.gameType, viewModel.tableauCardRevealFactor) {
                detectDragGestures(
                    onDragStart = { offset ->
                        // Hit detection for dragging
                        val allPiles = mutableListOf<Pile>()
                        allPiles.addAll(viewModel.tableau)
                        allPiles.addAll(viewModel.foundations)
                        allPiles.addAll(viewModel.freeCells)
                        if (viewModel.gameType == GameType.KLONDIKE) {
                            allPiles.addAll(viewModel.waste)
                        }

                        for (pile in allPiles) {
                            val pileIndex = when (pile.type) {
                                PileType.TABLEAU -> viewModel.tableau.indexOf(pile)
                                PileType.FOUNDATION -> viewModel.foundations.indexOf(pile)
                                PileType.FREE_CELL -> viewModel.freeCells.indexOf(pile)
                                else -> 0
                            }
                            val rect = getPileRect(pile, pileIndex)

                            if (pile.type == PileType.TABLEAU) {
                                // Specialized hit detection for tableau stacks
                                for (i in pile.cards.indices.reversed()) {
                                    val card = pile.cards[i]
                                    if (card.faceUp) {
                                        val cardY = rect.top + i * (cardHeight * viewModel.tableauCardRevealFactor)
                                        val cardRect = Rect(rect.left, cardY, rect.right, cardY + cardHeight)
                                        if (cardRect.contains(offset)) {
                                            val stack = viewModel.findValidSubStack(pile, i)
                                            draggingStack = stack
                                            draggingFromPile = pile
                                            dragStartOffset = offset - Offset(rect.left, cardY)
                                            dragPosition = offset - dragStartOffset
                                            return@detectDragGestures
                                        }
                                    }
                                }
                            } else {
                                if (rect.contains(offset) && pile.cards.isNotEmpty()) {
                                    draggingStack = listOf(pile.cards.last())
                                    draggingFromPile = pile
                                    dragStartOffset = offset - Offset(rect.left, rect.top)
                                    dragPosition = offset - dragStartOffset
                                    return@detectDragGestures
                                }
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        if (draggingStack != null) {
                            dragPosition += dragAmount
                            change.consume()
                        }
                    },
                    onDragEnd = {
                        val stack = draggingStack
                        val fromPile = draggingFromPile
                        if (stack != null && fromPile != null) {
                            val dropCenter = dragPosition + Offset(cardWidth / 2, cardHeight / 2)

                            // Try foundations
                            if (stack.size == 1) {
                                viewModel.foundations.forEachIndexed { index, foundation ->
                                    if (getPileRect(foundation, index).contains(dropCenter)) {
                                        if (viewModel.canPlaceOnFoundation(stack.first(), foundation)) {
                                            viewModel.moveToFoundation(fromPile, foundation)
                                            viewModel.saveGame(repository)
                                        }
                                    }
                                }
                            }

                            // Try tableau
                            viewModel.tableau.forEachIndexed { index, tableauPile ->
                                val rect = getPileRect(tableauPile, index)
                                // Extend tableau rect downwards for easier dropping
                                val dropRect = Rect(rect.left, rect.top, rect.right, screenHeight)
                                if (dropRect.contains(dropCenter)) {
                                    if (viewModel.canPlaceOnTableau(stack, tableauPile)) {
                                        viewModel.moveStackToTableau(fromPile, stack.toMutableList(), tableauPile)
                                        viewModel.saveGame(repository)
                                    }
                                }
                            }

                            // Try FreeCells
                            if (stack.size == 1 && viewModel.gameType == GameType.FREECELL) {
                                viewModel.freeCells.forEachIndexed { index, freeCell ->
                                    if (getPileRect(freeCell, index).contains(dropCenter)) {
                                        if (viewModel.canPlaceOnFreeCell(stack, freeCell)) {
                                            viewModel.moveStackToFreeCell(fromPile, stack.toMutableList(), freeCell)
                                            viewModel.saveGame(repository)
                                        }
                                    }
                                }
                            }
                        }
                        draggingStack = null
                        draggingFromPile = null
                    },
                    onDragCancel = {
                        draggingStack = null
                        draggingFromPile = null
                    }
                )
            }
    ) {
        if (cardWidth > 0) {
            val isPortrait = maxWidth < maxHeight

            // Controls
            if (isPortrait) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 16.dp)
                        .offset(y = maxHeight * 0.1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Button(
                        onClick = {
                            viewModel.newGame()
                            viewModel.saveGame(repository)
                        },
                        modifier = Modifier.height(40.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        Text("New Game", fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onOptionsClick,
                        modifier = Modifier.height(40.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        Text("Options", fontSize = 13.sp)
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = {
                        viewModel.newGame()
                        viewModel.saveGame(repository)
                    }) {
                        Text("New Game")
                    }
                    Button(onClick = onOptionsClick) {
                        Text("Options")
                    }
                }
            }

            // Top Row: Stock, Waste, Foundations (or FreeCells)
            Row(
                modifier = Modifier
                    .padding(start = leftMargin, top = topMargin)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                if (viewModel.gameType == GameType.KLONDIKE) {
                    // Stock
                    val stock = viewModel.stock.firstOrNull()
                    Box(
                        modifier = Modifier
                            .size(cardWidthDp, cardHeightDp)
                            .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .clickable {
                                viewModel.drawFromStock()
                                viewModel.saveGame(repository)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (stock?.cards?.isNotEmpty() == true) {
                            CardComponent(card = stock.cards.last(), modifier = Modifier.fillMaxSize())
                        } else {
                            Canvas(modifier = Modifier.size(24.dp)) {
                                drawCircle(Color.White, style = androidx.compose.ui.graphics.drawscope.Stroke(2f))
                            }
                        }
                    }

                    // Waste
                    val waste = viewModel.waste.firstOrNull()
                    Box(
                        modifier = Modifier.size(cardWidthDp, cardHeightDp)
                    ) {
                        waste?.cards?.takeLast(1)?.forEach { card ->
                            if (draggingStack?.contains(card) != true) {
                                CardComponent(card = card, modifier = Modifier.fillMaxSize())
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Foundations
                    viewModel.foundations.forEach { pile ->
                        Box(
                            modifier = Modifier
                                .size(cardWidthDp, cardHeightDp)
                                .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        ) {
                            pile.topCard()?.let { card ->
                                if (draggingStack?.contains(card) != true) {
                                    CardComponent(card = card, modifier = Modifier.fillMaxSize())
                                }
                            }
                        }
                    }
                } else if (viewModel.gameType == GameType.FREECELL) {
                    // FreeCells
                    viewModel.freeCells.forEach { pile ->
                        Box(
                            modifier = Modifier
                                .size(cardWidthDp, cardHeightDp)
                                .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        ) {
                            pile.topCard()?.let { card ->
                                if (draggingStack?.contains(card) != true) {
                                    CardComponent(card = card, modifier = Modifier.fillMaxSize())
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Foundations
                    viewModel.foundations.forEach { pile ->
                        Box(
                            modifier = Modifier
                                .size(cardWidthDp, cardHeightDp)
                                .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        ) {
                            pile.topCard()?.let { card ->
                                if (draggingStack?.contains(card) != true) {
                                    CardComponent(card = card, modifier = Modifier.fillMaxSize())
                                }
                            }
                        }
                    }
                }
            }

            // Tableau
            Row(
                modifier = Modifier
                    .padding(start = leftMargin, top = topMargin + cardHeightDp + 24.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                viewModel.tableau.forEach { pile ->
                    Box(
                        modifier = Modifier
                            .width(cardWidthDp)
                            .fillMaxHeight()
                    ) {
                        pile.cards.forEachIndexed { index, card ->
                            if (draggingStack?.contains(card) != true) {
                                val revealOffset = index * (cardHeight * viewModel.tableauCardRevealFactor)
                                val offsetDp = with(density) { revealOffset.toDp() }
                                CardComponent(
                                    card = card,
                                    modifier = Modifier
                                        .offset(y = offsetDp)
                                        .size(cardWidthDp, cardHeightDp)
                                        .pointerInput(card, pile) {
                                            detectTapGestures(
                                                onTap = {
                                                    if (card == pile.topCard()) {
                                                        viewModel.autoMoveCard(card, pile)
                                                        viewModel.saveGame(repository)
                                                    }
                                                },
                                                onDoubleTap = {
                                                    if (card == pile.topCard()) {
                                                        viewModel.autoMoveCard(card, pile)
                                                        viewModel.saveGame(repository)
                                                    }
                                                }
                                            )
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Win state overlay
        if (viewModel.checkWin()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🎉 You Win!", color = Color.Yellow, fontSize = 48.sp)
            }
        }

        // Draggable stack
        draggingStack?.let { stack ->
            Box(
                modifier = Modifier
                    .offset { IntOffset(dragPosition.x.roundToInt(), dragPosition.y.roundToInt()) }
            ) {
                stack.forEachIndexed { index, card ->
                    val revealOffset = index * (cardHeight * viewModel.tableauCardRevealFactor)
                    val offsetDp = with(density) { revealOffset.toDp() }
                    CardComponent(
                        card = card,
                        modifier = Modifier
                            .offset(y = offsetDp)
                            .size(cardWidthDp, cardHeightDp)
                    )
                }
            }
        }
    }
}
