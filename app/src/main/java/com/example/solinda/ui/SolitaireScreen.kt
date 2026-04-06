package com.example.solinda.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var autoScrollSpeed by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(autoScrollSpeed) {
        if (autoScrollSpeed != 0f) {
            while (true) {
                scrollState.scrollBy(autoScrollSpeed)
                delay(16)
            }
        }
    }

    val isLandscape = screenWidth > screenHeight
    val cardWidth = remember(screenWidth, screenHeight, viewModel.gameType, viewModel.leftMargin, viewModel.rightMargin, viewModel.leftMarginLandscape, viewModel.rightMarginLandscape) {
        if (screenWidth > 0) {
            val numPiles = if (viewModel.gameType == GameType.FREECELL) 8 else 7
            val leftMarginPx = with(density) { (if (isLandscape) viewModel.leftMarginLandscape else viewModel.leftMargin).dp.toPx() }
            val rightMarginPx = with(density) { (if (isLandscape) viewModel.rightMarginLandscape else viewModel.rightMargin).dp.toPx() }
            val totalSpacing = (numPiles - 1) * 2f // 2 pixels spacing
            (screenWidth - leftMarginPx - rightMarginPx - totalSpacing) / numPiles
        } else 0f
    }
    val cardHeight = remember(cardWidth, isLandscape) {
        if (isLandscape) cardWidth * 1.2f else cardWidth * 1.8f
    }

    var draggingStack by remember { mutableStateOf<List<Card>?>(null) }
    var draggingFromPile by remember { mutableStateOf<Pile?>(null) }
    var dragPosition by remember { mutableStateOf(Offset.Zero) }
    var dragStartOffset by remember { mutableStateOf(Offset.Zero) }

    val animatingCards = remember { mutableStateListOf<Card>() }

    val leftMargin = (if (isLandscape) viewModel.leftMarginLandscape else viewModel.leftMargin).dp
    val spacingPx = 2f
    val spacing = with(density) { spacingPx.toDp() }

    val cardWidthDp = with(density) { cardWidth.toDp() }
    val cardHeightDp = with(density) { cardHeight.toDp() }

    fun getPileX(pile: Pile, index: Int): Float {
        return with(density) {
            val startX = leftMargin.toPx()
            val pileSpacing = (cardWidth + spacingPx)
            when (pile.type) {
                PileType.STOCK -> startX
                PileType.WASTE -> startX + pileSpacing
                PileType.FOUNDATION -> {
                    if (viewModel.gameType == GameType.FREECELL) {
                        startX + (4 + index) * pileSpacing
                    } else {
                        startX + (3 + index) * pileSpacing
                    }
                }
                PileType.TABLEAU -> startX + index * pileSpacing
                PileType.FREE_CELL -> startX + index * pileSpacing
            }
        }
    }

    fun getPileY(pile: Pile, index: Int, topMarginPx: Float, includeScroll: Boolean = true): Float {
        return if (pile.type == PileType.TABLEAU) {
            val scrollOffset = if (includeScroll) scrollState.value.toFloat() else 0f
            topMarginPx + cardHeight + with(density) { 24.dp.toPx() } - scrollOffset
        } else {
            topMarginPx
        }
    }

    fun getPileRect(pile: Pile, index: Int, topMarginPx: Float, includeScroll: Boolean = true): Rect {
        val x = getPileX(pile, index)
        val y = getPileY(pile, index, topMarginPx, includeScroll)
        return Rect(x, y, x + cardWidth, y + cardHeight)
    }

    fun animateCardMove(card: Card, startPos: Offset, endPos: Offset, onComplete: () -> Unit = {}) {
        coroutineScope.launch {
            val animatedCard = card.copy()
            animatingCards.add(animatedCard)
            animatedCard.x = startPos.x
            animatedCard.y = startPos.y
            val animatable = Animatable(startPos, Offset.VectorConverter)
            animatable.animateTo(
                endPos,
                animationSpec = tween(durationMillis = Constants.ANIMATION_DURATION_MS)
            ) {
                animatedCard.x = value.x
                animatedCard.y = value.y
            }
            animatingCards.remove(animatedCard)
            onComplete()
        }
    }

    fun handleAutoComplete(topMarginPx: Float) {
        if (viewModel.gameType == GameType.FREECELL || viewModel.isGameWinnable()) {
            val result = viewModel.autoMoveToFoundation(skipModelUpdate = true)
            if (result != null) {
                val (card, fromPile, targetPile) = result
                val fromIndex = when (fromPile.type) {
                    PileType.TABLEAU -> viewModel.tableau.indexOf(fromPile)
                    PileType.FREE_CELL -> viewModel.freeCells.indexOf(fromPile)
                    PileType.WASTE -> viewModel.waste.indexOf(fromPile)
                    else -> 0
                }
                val cardIndex = fromPile.cards.size - 1
                val startX = getPileX(fromPile, fromIndex) + (if (fromPile.type == PileType.WASTE) cardIndex.coerceAtLeast(0).coerceAtMost(2) * with(density) { 20.dp.toPx() } else 0f)
                val startY = getPileY(fromPile, fromIndex, topMarginPx, includeScroll = true) + (if (fromPile.type == PileType.TABLEAU) cardIndex * (cardHeight * viewModel.tableauCardRevealFactor) else 0f)

                val targetIndex = viewModel.foundations.indexOf(targetPile)
                val endX = getPileX(targetPile, targetIndex)
                val endY = getPileY(targetPile, targetIndex, topMarginPx, includeScroll = true)

                animateCardMove(card, Offset(startX, startY), Offset(endX, endY)) {
                    viewModel.autoMoveToFoundation(skipModelUpdate = false)
                    handleAutoComplete(topMarginPx)
                    viewModel.saveGame(repository)
                }
            }
        }
    }

    fun handleAutoMove(card: Card, fromPile: Pile, pileIndex: Int, cardIndex: Int, topMarginPx: Float) {
        val startX = getPileX(fromPile, pileIndex) + (if (fromPile.type == PileType.WASTE) cardIndex.coerceAtLeast(0).coerceAtMost(2) * with(density) { 20.dp.toPx() } else 0f)
        val startY = getPileY(fromPile, pileIndex, topMarginPx, includeScroll = true) + (if (fromPile.type == PileType.TABLEAU) cardIndex * (cardHeight * viewModel.tableauCardRevealFactor) else 0f)

        val targetPile = viewModel.autoMoveCard(card, fromPile, skipModelUpdate = true)
        if (targetPile != null) {
            val targetIndex = when (targetPile.type) {
                PileType.TABLEAU -> viewModel.tableau.indexOf(targetPile)
                PileType.FOUNDATION -> viewModel.foundations.indexOf(targetPile)
                else -> 0
            }
            val endX = getPileX(targetPile, targetIndex)
            val endY = getPileY(targetPile, targetIndex, topMarginPx, includeScroll = true) + if (targetPile.type == PileType.TABLEAU) (targetPile.cards.size) * (cardHeight * viewModel.tableauCardRevealFactor) else 0f

            animateCardMove(card, Offset(startX, startY), Offset(endX, endY)) {
                // Perform model update AFTER animation
                viewModel.autoMoveCard(card, fromPile, skipModelUpdate = false)
                handleAutoComplete(topMarginPx)
                viewModel.saveGame(repository)
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B6623))
            .onGloballyPositioned {
                screenWidth = it.size.width.toFloat()
                screenHeight = it.size.height.toFloat()
            }
    ) {
        val topMarginPx = with(density) { (if (isLandscape) 16.dp else (this@BoxWithConstraints.maxHeight * 0.1f + 112.dp)).toPx() }

        // Interaction layer
        Box(modifier = Modifier.fillMaxSize()
            .pointerInput(cardWidth, cardHeight, viewModel.gameType, viewModel.tableauCardRevealFactor, topMarginPx) {
                detectTapGestures(
                    onTap = { offset ->
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
                            val rect = getPileRect(pile, pileIndex, topMarginPx)

                            if (pile.type == PileType.TABLEAU) {
                                for (i in pile.cards.indices.reversed()) {
                                    val card = pile.cards[i]
                                    val cardY = rect.top + i * (cardHeight * viewModel.tableauCardRevealFactor)
                                    val cardRect = Rect(rect.left, cardY, rect.right, cardY + cardHeight)
                                    if (cardRect.contains(offset)) {
                                        if (card == pile.topCard()) {
                                            if (card.faceUp) {
                                                handleAutoMove(card, pile, pileIndex, i, topMarginPx)
                                            } else {
                                                card.faceUp = true
                                                viewModel.saveGame(repository)
                                            }
                                        }
                                        return@detectTapGestures
                                    }
                                }
                            } else if (pile.type == PileType.WASTE) {
                                val cards = pile.cards.takeLast(3)
                                for (i in cards.indices.reversed()) {
                                    val card = cards[i]
                                    val wasteIndex = (pile.cards.size - cards.size) + i
                                    val cardX = rect.left + i * with(density) { 20.dp.toPx() }
                                    val cardRect = Rect(cardX, rect.top, cardX + cardWidth, rect.bottom)
                                    if (cardRect.contains(offset)) {
                                        if (card == pile.topCard()) {
                                            handleAutoMove(card, pile, 0, wasteIndex, topMarginPx)
                                        }
                                        return@detectTapGestures
                                    }
                                }
                            } else {
                                if (rect.contains(offset) && pile.cards.isNotEmpty()) {
                                    val card = pile.cards.last()
                                    handleAutoMove(card, pile, pileIndex, 0, topMarginPx)
                                    return@detectTapGestures
                                }
                            }
                        }
                    }
                )
            }
            .pointerInput(cardWidth, cardHeight, viewModel.gameType, viewModel.tableauCardRevealFactor, topMarginPx) {
                detectDragGestures(
                    onDragStart = { offset ->
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
                            val rect = getPileRect(pile, pileIndex, topMarginPx)

                            if (pile.type == PileType.TABLEAU) {
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

                            val y = change.position.y
                            val edgeThreshold = with(density) { 60.dp.toPx() }
                            val speed = with(density) { 10.dp.toPx() }
                            autoScrollSpeed = when {
                                y < edgeThreshold -> -speed
                                y > screenHeight - edgeThreshold -> speed
                                else -> 0f
                            }

                            change.consume()
                        } else {
                            coroutineScope.launch {
                                scrollState.scrollBy(-dragAmount.y)
                            }
                        }
                    },
                    onDragEnd = {
                        autoScrollSpeed = 0f
                        val stack = draggingStack
                        val fromPile = draggingFromPile
                        if (stack != null && fromPile != null) {
                            val dropCenter = dragPosition + Offset(cardWidth / 2, cardHeight / 2)

                            // Try foundations
                            if (stack.size == 1) {
                                viewModel.foundations.forEachIndexed { index, foundation ->
                                    if (getPileRect(foundation, index, topMarginPx).contains(dropCenter)) {
                                        if (viewModel.canPlaceOnFoundation(stack.first(), foundation)) {
                                            viewModel.moveToFoundation(fromPile, foundation)
                                            handleAutoComplete(topMarginPx)
                                            viewModel.saveGame(repository)
                                        }
                                    }
                                }
                            }

                            // Try tableau
                            viewModel.tableau.forEachIndexed { index, tableauPile ->
                                val rect = getPileRect(tableauPile, index, topMarginPx)
                                val dropRect = Rect(rect.left, rect.top, rect.right, screenHeight)
                                if (dropRect.contains(dropCenter)) {
                                    if (viewModel.canPlaceOnTableau(stack, tableauPile)) {
                                        viewModel.moveStackToTableau(fromPile, stack.toMutableList(), tableauPile)
                                        handleAutoComplete(topMarginPx)
                                        viewModel.saveGame(repository)
                                    }
                                }
                            }

                            // Try FreeCells
                            if (stack.size == 1 && viewModel.gameType == GameType.FREECELL) {
                                viewModel.freeCells.forEachIndexed { index, freeCell ->
                                    if (getPileRect(freeCell, index, topMarginPx).contains(dropCenter)) {
                                        if (viewModel.canPlaceOnFreeCell(stack, freeCell)) {
                                            viewModel.moveStackToFreeCell(fromPile, stack.toMutableList(), freeCell)
                                            handleAutoComplete(topMarginPx)
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
                        autoScrollSpeed = 0f
                        draggingStack = null
                        draggingFromPile = null
                    }
                )
            }
        ) {
            if (cardWidth > 0) {
                val topMarginDp = with(density) { topMarginPx.toDp() }

                // Top area: Stock, Waste, FreeCells, Foundations
                Box(
                    modifier = Modifier
                        .padding(top = topMarginDp)
                        .fillMaxWidth()
                        .height(cardHeightDp)
                ) {
                    if (viewModel.gameType == GameType.KLONDIKE) {
                        // Stock
                        val stock = viewModel.stock.firstOrNull()
                        val stockX = with(density) { getPileX(stock ?: Pile(PileType.STOCK, mutableListOf()), 0).toDp() }
                        Box(
                            modifier = Modifier
                                .offset(x = stockX)
                                .size(cardWidthDp, cardHeightDp)
                                .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .clickable {
                                    val stockPile = viewModel.stock.firstOrNull() ?: return@clickable
                                    val wastePile = viewModel.waste.firstOrNull() ?: return@clickable
                                    val startX = getPileX(stockPile, 0)
                                    val startY = getPileY(stockPile, 0, topMarginPx)
                                    val drawnCards = viewModel.drawFromStock()
                                    val targetX = getPileX(wastePile, 0)
                                    val targetY = getPileY(wastePile, 0, topMarginPx)

                                    drawnCards.forEachIndexed { i, card ->
                                        val endX = targetX + i.coerceAtMost(2) * with(density) { 20.dp.toPx() }
                                        animateCardMove(card, Offset(startX, startY), Offset(endX, targetY))
                                    }
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
                        val wasteX = with(density) { getPileX(waste ?: Pile(PileType.WASTE, mutableListOf()), 0).toDp() }
                        Box(modifier = Modifier.offset(x = wasteX).width(cardWidthDp + (2 * 20).dp).height(cardHeightDp)) {
                            waste?.cards?.takeLast(3)?.forEachIndexed { index, card ->
                                if (draggingStack?.contains(card) != true && animatingCards.none { it.suit == card.suit && it.rank == card.rank }) {
                                    CardComponent(
                                        card = card,
                                        modifier = Modifier
                                            .offset(x = (index * 20).dp)
                                            .size(cardWidthDp, cardHeightDp)
                                    )
                                }
                            }
                        }

                        // Foundations
                        viewModel.foundations.forEachIndexed { index, pile ->
                            val fx = with(density) { getPileX(pile, index).toDp() }
                            Box(
                                modifier = Modifier
                                    .offset(x = fx)
                                    .size(cardWidthDp, cardHeightDp)
                                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            ) {
                                pile.topCard()?.let { card ->
                                    if (draggingStack?.contains(card) != true && animatingCards.none { it.suit == card.suit && it.rank == card.rank }) {
                                        CardComponent(card = card, modifier = Modifier.fillMaxSize())
                                    }
                                }
                            }
                        }
                    } else if (viewModel.gameType == GameType.FREECELL) {
                        // FreeCells
                        viewModel.freeCells.forEachIndexed { index, pile ->
                            val fcx = with(density) { getPileX(pile, index).toDp() }
                            Box(
                                modifier = Modifier
                                    .offset(x = fcx)
                                    .size(cardWidthDp, cardHeightDp)
                                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            ) {
                                pile.topCard()?.let { card ->
                                    if (draggingStack?.contains(card) != true && animatingCards.none { it.suit == card.suit && it.rank == card.rank }) {
                                        CardComponent(card = card, modifier = Modifier.fillMaxSize())
                                    }
                                }
                            }
                        }

                        // Foundations
                        viewModel.foundations.forEachIndexed { index, pile ->
                            val fx = with(density) { getPileX(pile, index).toDp() }
                            Box(
                                modifier = Modifier
                                    .offset(x = fx)
                                    .size(cardWidthDp, cardHeightDp)
                                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            ) {
                                pile.topCard()?.let { card ->
                                    if (draggingStack?.contains(card) != true && animatingCards.none { it.suit == card.suit && it.rank == card.rank }) {
                                        CardComponent(card = card, modifier = Modifier.fillMaxSize())
                                    }
                                }
                            }
                        }
                    }
                }

                // Tableau
                Box(
                    modifier = Modifier
                        .padding(top = topMarginDp + cardHeightDp + 24.dp)
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(start = leftMargin)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing)
                    ) {
                        viewModel.tableau.forEachIndexed { pileIndex, pile ->
                            val tableauHeight = cardHeight + (pile.cards.size.coerceAtLeast(1) - 1) * (cardHeight * viewModel.tableauCardRevealFactor) + with(density) { 200.dp.toPx() }
                            Box(modifier = Modifier.width(cardWidthDp).height(with(density) { tableauHeight.toDp() })) {
                                pile.cards.forEachIndexed { cardIndex, card ->
                                    if (draggingStack?.contains(card) != true && animatingCards.none { it.suit == card.suit && it.rank == card.rank }) {
                                        val revealOffset = cardIndex * (cardHeight * viewModel.tableauCardRevealFactor)

                                        val isDimmed = if (viewModel.gameType == GameType.FREECELL) {
                                            val stack = pile.cards.subList(cardIndex, pile.cards.size)
                                            val isValidSequence = viewModel.isValidTableauStack(stack)

                                            if (!isValidSequence) {
                                                true
                                            } else {
                                                val emptyFreeCells = viewModel.freeCells.count { it.isEmpty() }
                                                val emptyTableauPiles = viewModel.tableau.count { it.isEmpty() && it != pile }
                                                val maxStackSize = (1 + emptyFreeCells) * (1 shl emptyTableauPiles)
                                                stack.size > maxStackSize
                                            }
                                        } else false

                                        CardComponent(
                                            card = card,
                                            modifier = Modifier
                                                .offset(y = with(density) { revealOffset.toDp() })
                                                .size(cardWidthDp, cardHeightDp),
                                            isDimmed = isDimmed
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Controls
                val isPortrait = this@BoxWithConstraints.maxWidth < this@BoxWithConstraints.maxHeight
                if (isPortrait) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 16.dp)
                            .offset(y = this@BoxWithConstraints.maxHeight * 0.1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Button(onClick = {
                            viewModel.newGame()
                            viewModel.saveGame(repository)
                            coroutineScope.launch { scrollState.scrollTo(0) }
                        }, modifier = Modifier.height(40.dp)) {
                            Text("New Game", fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onOptionsClick, modifier = Modifier.height(40.dp)) {
                            Text("Options", fontSize = 13.sp)
                        }
                    }
                } else {
                    if (viewModel.gameType == GameType.FREECELL) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 16.dp, bottom = 16.dp),
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(onClick = {
                                viewModel.newGame()
                                viewModel.saveGame(repository)
                                coroutineScope.launch { scrollState.scrollTo(0) }
                            }) { Text("New Game") }
                            Button(onClick = onOptionsClick) { Text("Options") }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(onClick = { viewModel.newGame(); viewModel.saveGame(repository) }) { Text("New Game") }
                            Button(onClick = onOptionsClick) { Text("Options") }
                        }
                    }
                }
            }

            // Win state overlay
            if (viewModel.checkWin()) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable { }, contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🎉 You Win!", color = Color.Yellow, fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = {
                            viewModel.newGame()
                            viewModel.saveGame(repository)
                            coroutineScope.launch { scrollState.scrollTo(0) }
                        }) {
                            Text("New Game", fontSize = 20.sp)
                        }
                    }
                }
            }

            // Animating cards layer
            animatingCards.forEach { card ->
                Box(modifier = Modifier.offset { IntOffset(card.x.roundToInt(), card.y.roundToInt()) }) {
                    CardComponent(card = card, modifier = Modifier.size(cardWidthDp, cardHeightDp))
                }
            }

            // Draggable stack
            draggingStack?.let { stack ->
                Box(modifier = Modifier.offset { IntOffset(dragPosition.x.roundToInt(), dragPosition.y.roundToInt()) }) {
                    stack.forEachIndexed { index, card ->
                        val revealOffset = index * (cardHeight * viewModel.tableauCardRevealFactor)
                        CardComponent(card = card, modifier = Modifier.offset(y = with(density) { revealOffset.toDp() }).size(cardWidthDp, cardHeightDp))
                    }
                }
            }

            // Scrollbar
            val maxTableauSize = viewModel.tableau.maxOfOrNull { it.cards.size } ?: 0
            val contentHeight = cardHeight + maxTableauSize * (cardHeight * viewModel.tableauCardRevealFactor) + with(density) { 200.dp.toPx() }
            if (contentHeight > screenHeight) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 2.dp)
                        .width(4.dp)
                        .fillMaxHeight(0.5f)
                        .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                ) {
                    val scrollFraction = scrollState.value.toFloat() / (scrollState.maxValue.toFloat().coerceAtLeast(1f))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.2f)
                            .offset(y = with(density) { (scrollFraction * (screenHeight * 0.5f * 0.8f)).toDp() })
                            .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(2.dp))
                    )
                }
            }
        }
    }
}
