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

enum class InteractionType {
    NONE, DRAGGING_CARD, SCROLLING, IGNORE
}

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
    var interactionType by remember { mutableStateOf(InteractionType.NONE) }

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

    fun getPileRect(pile: Pile, index: Int, topMarginPx: Float): Rect {
        val x = getPileX(pile, index)
        val y = when (pile.type) {
            PileType.TABLEAU -> topMarginPx + cardHeight + with(density) { 6.dp.toPx() } - scrollState.value
            else -> topMarginPx
        }
        return Rect(x, y, x + cardWidth, y + cardHeight)
    }

    fun handleAutoComplete(topMarginPx: Float) {
        // Auto-complete logic
        coroutineScope.launch {
            var moved: Boolean
            do {
                moved = false
                val allPiles = mutableListOf<Pile>()
                allPiles.addAll(viewModel.tableau)
                if (viewModel.gameType == GameType.KLONDIKE) {
                    allPiles.addAll(viewModel.waste)
                }
                allPiles.addAll(viewModel.freeCells)

                for (fromPile in allPiles) {
                    val card = fromPile.topCard() ?: continue
                    if (!card.faceUp) continue

                    for (foundation in viewModel.foundations) {
                        if (viewModel.canPlaceOnFoundation(card, foundation)) {
                            val fromX = getPileX(fromPile, when (fromPile.type) {
                                PileType.TABLEAU -> viewModel.tableau.indexOf(fromPile)
                                PileType.FREE_CELL -> viewModel.freeCells.indexOf(fromPile)
                                else -> 0
                            })
                            val fromY = when (fromPile.type) {
                                PileType.TABLEAU -> topMarginPx + cardHeight + with(density) { 6.dp.toPx() } + (fromPile.cards.size - 1) * (cardHeight * viewModel.tableauCardRevealFactor) - scrollState.value
                                PileType.WASTE -> topMarginPx
                                else -> topMarginPx
                            }
                            val toX = getPileX(foundation, viewModel.foundations.indexOf(foundation))
                            val toY = topMarginPx

                            val animCard = card.copy()
                            animCard.x = fromX
                            animCard.y = fromY
                            animatingCards.add(animCard)

                            viewModel.moveToFoundation(fromPile, foundation)
                            moved = true

                            launch {
                                val animX = Animatable(fromX)
                                val animY = Animatable(fromY)
                                launch { animX.animateTo(toX, tween(Constants.ANIMATION_DURATION_MS)) { animCard.x = value } }
                                launch { animY.animateTo(toY, tween(Constants.ANIMATION_DURATION_MS)) { animCard.y = value } }
                                delay(Constants.ANIMATION_DURATION_MS.toLong())
                                animatingCards.remove(animCard)
                            }
                            break
                        }
                    }
                    if (moved) break
                }
                if (moved) delay(Constants.ANIMATION_DURATION_MS.toLong() + 20)
            } while (moved)
        }
    }

    fun handleAutoMove(card: Card, fromPile: Pile, pileIndex: Int, cardIndex: Int, topMarginPx: Float) {
        // Priority 1: Foundations
        viewModel.foundations.forEachIndexed { fIndex, foundation ->
            if (viewModel.canPlaceOnFoundation(card, foundation)) {
                val fromX = getPileX(fromPile, pileIndex)
                val fromY = when (fromPile.type) {
                    PileType.TABLEAU -> topMarginPx + cardHeight + with(density) { 6.dp.toPx() } + cardIndex * (cardHeight * viewModel.tableauCardRevealFactor) - scrollState.value
                    PileType.WASTE -> topMarginPx
                    else -> topMarginPx
                }
                val toX = getPileX(foundation, fIndex)
                val toY = topMarginPx

                val animCard = card.copy()
                animCard.x = fromX
                animCard.y = fromY
                animatingCards.add(animCard)

                viewModel.moveToFoundation(fromPile, foundation)
                viewModel.saveGame(repository)
                handleAutoComplete(topMarginPx)

                coroutineScope.launch {
                    val animX = Animatable(fromX)
                    val animY = Animatable(fromY)
                    launch { animX.animateTo(toX, tween(Constants.ANIMATION_DURATION_MS)) { animCard.x = value } }
                    launch { animY.animateTo(toY, tween(Constants.ANIMATION_DURATION_MS)) { animCard.y = value } }
                    delay(Constants.ANIMATION_DURATION_MS.toLong())
                    animatingCards.remove(animCard)
                }
                return
            }
        }

        // Priority 2: FreeCells
        if (viewModel.gameType == GameType.FREECELL) {
            viewModel.freeCells.forEachIndexed { fcIndex, freeCell ->
                if (viewModel.canPlaceOnFreeCell(listOf(card), freeCell)) {
                    val fromX = getPileX(fromPile, pileIndex)
                    val fromY = topMarginPx + cardHeight + with(density) { 6.dp.toPx() } + cardIndex * (cardHeight * viewModel.tableauCardRevealFactor) - scrollState.value
                    val toX = getPileX(freeCell, fcIndex)
                    val toY = topMarginPx

                    val animCard = card.copy()
                    animCard.x = fromX
                    animCard.y = fromY
                    animatingCards.add(animCard)

                    viewModel.moveStackToFreeCell(fromPile, mutableListOf(card), freeCell)
                    viewModel.saveGame(repository)
                    handleAutoComplete(topMarginPx)

                    coroutineScope.launch {
                        val animX = Animatable(fromX)
                        val animY = Animatable(fromY)
                        launch { animX.animateTo(toX, tween(Constants.ANIMATION_DURATION_MS)) { animCard.x = value } }
                        launch { animY.animateTo(toY, tween(Constants.ANIMATION_DURATION_MS)) { animCard.y = value } }
                        delay(Constants.ANIMATION_DURATION_MS.toLong())
                        animatingCards.remove(animCard)
                    }
                    return
                }
            }
        }

        // Priority 3: Tableau (standard auto-move)
        viewModel.tableau.forEachIndexed { tIndex, toTableau ->
            if (toTableau != fromPile && viewModel.canPlaceOnTableau(listOf(card), toTableau)) {
                val fromX = getPileX(fromPile, pileIndex)
                val fromY = when (fromPile.type) {
                    PileType.TABLEAU -> topMarginPx + cardHeight + with(density) { 6.dp.toPx() } + cardIndex * (cardHeight * viewModel.tableauCardRevealFactor) - scrollState.value
                    PileType.WASTE -> topMarginPx
                    PileType.FREE_CELL -> topMarginPx
                    else -> topMarginPx
                }
                val toX = getPileX(toTableau, tIndex)
                val toY = topMarginPx + cardHeight + with(density) { 6.dp.toPx() } + toTableau.cards.size * (cardHeight * viewModel.tableauCardRevealFactor) - scrollState.value

                val animCard = card.copy()
                animCard.x = fromX
                animCard.y = fromY
                animatingCards.add(animCard)

                viewModel.moveStackToTableau(fromPile, mutableListOf(card), toTableau)
                viewModel.saveGame(repository)
                handleAutoComplete(topMarginPx)

                coroutineScope.launch {
                    val animX = Animatable(fromX)
                    val animY = Animatable(fromY)
                    launch { animX.animateTo(toX, tween(Constants.ANIMATION_DURATION_MS)) { animCard.x = value } }
                    launch { animY.animateTo(toY, tween(Constants.ANIMATION_DURATION_MS)) { animCard.y = value } }
                    delay(Constants.ANIMATION_DURATION_MS.toLong())
                    animatingCards.remove(animCard)
                }
                return
            }
        }
    }

    fun handleDoubleTap(card: Card, fromPile: Pile, pileIndex: Int, cardIndex: Int, topMarginPx: Float) {
        // Double tap always tries foundations first, then freecells
        handleAutoMove(card, fromPile, pileIndex, cardIndex, topMarginPx)
    }

    fun handleStockClick(topMarginPx: Float) {
        viewModel.drawFromStock()
        viewModel.saveGame(repository)
        handleAutoComplete(topMarginPx)
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
        val topMarginDp = with(density) { topMarginPx.toDp() }

        Box(modifier = Modifier.fillMaxSize()) {
            if (cardWidth > 0) {
                // Top area: Stock, Waste, FreeCells, Foundations
                Box(
                    modifier = Modifier
                        .padding(top = topMarginDp)
                        .fillMaxWidth()
                        .height(cardHeightDp)
                ) {
                    if (viewModel.gameType == GameType.KLONDIKE) {
                        // Stock
                        viewModel.stock.firstOrNull()?.let { stockPile ->
                            Box(
                                modifier = Modifier
                                    .offset(x = with(density) { getPileX(stockPile, 0).toDp() })
                                    .size(cardWidthDp, cardHeightDp)
                                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            ) {
                                stockPile.topCard()?.let { card ->
                                    CardComponent(card = card, modifier = Modifier.fillMaxSize())
                                }
                            }
                        }

                        // Waste
                        viewModel.waste.firstOrNull()?.let { wastePile ->
                            Box(
                                modifier = Modifier
                                    .offset(x = with(density) { getPileX(wastePile, 0).toDp() })
                                    .size(cardWidthDp * 2.5f, cardHeightDp)
                            ) {
                                val cards = wastePile.cards.takeLast(3)
                                cards.forEachIndexed { index, card ->
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
                        }
                    } else if (viewModel.gameType == GameType.FREECELL) {
                        // FreeCells
                        viewModel.freeCells.forEachIndexed { index, pile ->
                            Box(
                                modifier = Modifier
                                    .offset(x = with(density) { getPileX(pile, index).toDp() })
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

                    // Foundations
                    viewModel.foundations.forEachIndexed { index, pile ->
                        Box(
                            modifier = Modifier
                                .offset(x = with(density) { getPileX(pile, index).toDp() })
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

                // Tableau
                Box(
                    modifier = Modifier
                        .padding(top = topMarginDp + cardHeightDp + 6.dp)
                        .fillMaxSize()
                        .verticalScroll(scrollState, enabled = false)
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
            }

            // Interaction layer
            Box(modifier = Modifier
                .fillMaxSize()
                .pointerInput(cardWidth, cardHeight, viewModel.gameType, viewModel.tableauCardRevealFactor, topMarginPx) {
                    detectTapGestures(
                        onDoubleTap = { offset ->
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
                                            if (card == pile.topCard() && card.faceUp) {
                                                handleDoubleTap(card, pile, pileIndex, i, topMarginPx)
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
                                                handleDoubleTap(card, pile, 0, wasteIndex, topMarginPx)
                                            }
                                            return@detectTapGestures
                                        }
                                    }
                                } else {
                                    if (rect.contains(offset) && pile.cards.isNotEmpty()) {
                                        val card = pile.cards.last()
                                        handleDoubleTap(card, pile, pileIndex, 0, topMarginPx)
                                        return@detectTapGestures
                                    }
                                }
                            }
                        },
                        onTap = { offset ->
                            // 1. Check Stock
                            if (viewModel.gameType == GameType.KLONDIKE) {
                                val stockPile = viewModel.stock.firstOrNull()
                                if (stockPile != null) {
                                    val stockRect = getPileRect(stockPile, 0, topMarginPx)
                                    if (stockRect.contains(offset)) {
                                        handleStockClick(topMarginPx)
                                        return@detectTapGestures
                                    }
                                }
                            }

                            // 2. Check other piles
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
                                                    // In Klondike, tapping a face-down top card reveals it
                                                    if (viewModel.gameType == GameType.KLONDIKE) {
                                                        // Auto reveal
                                                        card.faceUp = true
                                                        viewModel.saveGame(repository)
                                                    }
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
                            // 1. Check if dragging in top area (non-scrollable area)
                            if (offset.y < topMarginPx + cardHeight + with(density) { 6.dp.toPx() }) {
                                // Check if hitting a card in foundations or freecells or stock/waste
                                val topPiles = mutableListOf<Pile>()
                                topPiles.addAll(viewModel.foundations)
                                topPiles.addAll(viewModel.freeCells)
                                if (viewModel.gameType == GameType.KLONDIKE) {
                                    topPiles.addAll(viewModel.waste)
                                }

                                for (pile in topPiles) {
                                    val pileIndex = when (pile.type) {
                                        PileType.FOUNDATION -> viewModel.foundations.indexOf(pile)
                                        PileType.FREE_CELL -> viewModel.freeCells.indexOf(pile)
                                        else -> 0
                                    }
                                    val rect = getPileRect(pile, pileIndex, topMarginPx)

                                    if (pile.type == PileType.WASTE) {
                                        val cards = pile.cards.takeLast(3)
                                        for (i in cards.indices.reversed()) {
                                            val card = cards[i]
                                            val cardX = rect.left + i * with(density) { 20.dp.toPx() }
                                            val cardRect = Rect(cardX, rect.top, cardX + cardWidth, rect.bottom)
                                            if (cardRect.contains(offset)) {
                                                if (card == pile.topCard()) {
                                                    draggingStack = listOf(card)
                                                    draggingFromPile = pile
                                                    dragStartOffset = offset - Offset(cardX, rect.top)
                                                    dragPosition = offset - dragStartOffset
                                                    interactionType = InteractionType.DRAGGING_CARD
                                                    return@detectDragGestures
                                                }
                                            }
                                        }
                                    } else {
                                        if (rect.contains(offset) && pile.cards.isNotEmpty()) {
                                            val card = pile.cards.last()
                                            draggingStack = listOf(card)
                                            draggingFromPile = pile
                                            dragStartOffset = offset - Offset(rect.left, rect.top)
                                            dragPosition = offset - dragStartOffset
                                            interactionType = InteractionType.DRAGGING_CARD
                                            return@detectDragGestures
                                        }
                                    }
                                }
                                // Otherwise, ignore drag in top area background
                                interactionType = InteractionType.IGNORE
                                return@detectDragGestures
                            }

                            // 2. Check if hitting a card in the tableau
                            viewModel.tableau.forEachIndexed { pileIndex, pile ->
                                val rect = getPileRect(pile, pileIndex, topMarginPx)
                                for (i in pile.cards.indices.reversed()) {
                                    val card = pile.cards[i]
                                    val cardY = rect.top + i * (cardHeight * viewModel.tableauCardRevealFactor)
                                    val cardRect = Rect(rect.left, cardY, rect.right, cardY + cardHeight)
                                    if (cardRect.contains(offset)) {
                                        if (card.faceUp) {
                                            val isMoveable = if (viewModel.gameType == GameType.FREECELL) {
                                                // Simplified check for "is moveable" in FreeCell:
                                                // It must be part of a valid sequence AND not dimmed
                                                val stack = pile.cards.subList(i, pile.cards.size)
                                                val isValidSequence = viewModel.isValidTableauStack(stack)
                                                if (isValidSequence) {
                                                    val emptyFreeCells = viewModel.freeCells.count { it.isEmpty() }
                                                    val emptyTableauPiles = viewModel.tableau.count { it.isEmpty() && it != pile }
                                                    val maxStackSize = (1 + emptyFreeCells) * (1 shl emptyTableauPiles)
                                                    stack.size <= maxStackSize
                                                } else false
                                            } else true // In Klondike, if faceUp, it's generally moveable if sub-stack is valid

                                            if (isMoveable) {
                                                val stack = viewModel.findValidSubStack(pile, i)
                                                if (stack.first() == card) {
                                                    draggingStack = stack
                                                    draggingFromPile = pile
                                                    dragStartOffset = offset - Offset(rect.left, cardY)
                                                    dragPosition = offset - dragStartOffset
                                                    interactionType = InteractionType.DRAGGING_CARD
                                                    return@detectDragGestures
                                                }
                                            }
                                        }
                                        // Hit a face-down card or unmoveable card
                                        interactionType = InteractionType.IGNORE
                                        return@detectDragGestures
                                    }
                                }
                            }

                            // 3. Otherwise, it's a background drag in the tableau area
                            interactionType = InteractionType.SCROLLING
                        },
                        onDrag = { change, dragAmount ->
                            when (interactionType) {
                                InteractionType.DRAGGING_CARD -> {
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
                                }
                                InteractionType.SCROLLING -> {
                                    coroutineScope.launch {
                                        scrollState.scrollBy(-dragAmount.y)
                                    }
                                    change.consume()
                                }
                                else -> { /* IGNORE or NONE */ }
                            }
                        },
                        onDragEnd = {
                            autoScrollSpeed = 0f
                            val stack = draggingStack
                            val fromPile = draggingFromPile
                            val currentInteraction = interactionType
                            interactionType = InteractionType.NONE

                            if (currentInteraction == InteractionType.DRAGGING_CARD && stack != null && fromPile != null) {
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
                            interactionType = InteractionType.NONE
                        }
                    )
                }
            )

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

            // Win state overlay
            if (viewModel.checkWin()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("You Win!", color = Color.White, fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(32.dp))
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
