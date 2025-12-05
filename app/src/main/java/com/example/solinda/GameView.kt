package com.example.solinda

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.graphics.scale
import androidx.core.graphics.toColorInt
import kotlin.math.abs

class GameView @JvmOverloads constructor(
    context: Context,
    private val viewModel: GameViewModel,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val VERTICAL_MARGIN = 40f
        private const val INTER_ROW_SPACING = 40f
        private const val INTER_CARD_SPACING = 20f
        private const val WASTE_CARD_REVEAL_FACTOR = 0.3f
    }

    private fun dpToPx(dp: Int): Float {
        return dp * resources.displayMetrics.density
    }

    private data class AnimationState(
        val card: Card,
        val startX: Float,
        val startY: Float,
        val endX: Float,
        val endY: Float,
        val animator: ValueAnimator
    )

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val isLandscape get() = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    private var calculatedCardWidth: Float = 0f
    private var calculatedCardHeight: Float = 0f

    private fun calculateCardLayout(numPiles: Int) {
        if (width == 0 || height == 0 || numPiles == 0) return

        val leftMarginPx = dpToPx(viewModel.leftMargin)
        val rightMarginPx = dpToPx(viewModel.rightMargin)

        val totalSpacing = (numPiles - 1) * INTER_CARD_SPACING
        val totalMargin = leftMarginPx + rightMarginPx
        val availableWidth = width - totalMargin - totalSpacing
        calculatedCardWidth = availableWidth / numPiles

        if (isLandscape) {
            val totalVerticalMargin = 2 * VERTICAL_MARGIN + INTER_ROW_SPACING
            val availableHeight = height - totalVerticalMargin
            // Landscape height needs to accommodate: 1 card height for the top row,
            // plus the fanned-out tableau. A typical Klondike game starts with 7 cards in the last pile.
            // The revealed part is TABLEAU_CARD_REVEAL_FACTOR. So, 1 card height + 6 * revealed height.
            // Total height needed is roughly (1 + 6 * 0.3) = 2.8 card heights.
            // Add another card height for the top row, so ~3.8. Let's use 4.0 for a safe margin.
            calculatedCardHeight = availableHeight / 4.0f
        } else {
            // Portrait mode: calculate height based on aspect ratio
            calculatedCardHeight = calculatedCardWidth * 1.4f // A more standard card aspect ratio
        }
    }

    private val tableauStartX get() = dpToPx(viewModel.leftMargin)

    // Drag and tap state
    private var dragStack: MutableList<Card>? = null
    private var dragFromPile: Pile? = null
    private var dragX = 0f
    private var dragY = 0f

    private val touchSlop by lazy { ViewConfiguration.get(context).scaledTouchSlop }
    private var downX = 0f
    private var downY = 0f
    private var isDragging = false
    private var potentialDragStack: MutableList<Card>? = null
    private var potentialDragPile: Pile? = null

    private var previousWasteState: List<Card> = emptyList()

    private var cardBackImage: Bitmap? = null
    private val cardImages = mutableMapOf<String, Bitmap>()
    private val activeAnimations = mutableListOf<AnimationState>()
    private var isAutoCompleting = false

    private fun loadCardImages() {
        if (width == 0 || height == 0) return // Don't load if view not measured

        val backId = resources.getIdentifier("back", "drawable", context.packageName)
        if (backId != 0) {
            val bitmap = BitmapFactory.decodeResource(resources, backId)
            if (bitmap != null) {
                cardBackImage = bitmap.scale(calculatedCardWidth.toInt(), calculatedCardHeight.toInt(), false)
            }
        }

        for (suit in Suit.entries) {
            for (rank in 1..13) {
                val card = Card(suit, rank)
                val id = resources.getIdentifier(card.imageName, "drawable", context.packageName)
                if (id != 0) {
                    val bitmap = BitmapFactory.decodeResource(resources, id)
                    if (bitmap != null) {
                        cardImages[card.imageName] = bitmap.scale(calculatedCardWidth.toInt(), calculatedCardHeight.toInt(), false)
                    }
                }
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w == 0 || h == 0) return
        calculateCardLayout(viewModel.tableau.size)
        loadCardImages()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor("#0B6623".toColorInt())
        val animatingCards = activeAnimations.map { it.card }

        if (viewModel.gameType == GameType.KLONDIKE) {
            drawStockAndWaste(canvas, animatingCards)
        } else {
            drawFreeCells(canvas, animatingCards)
        }
        drawFoundations(canvas, animatingCards)
        drawTableau(canvas, animatingCards)

        // Draw currently dragging cards
        dragStack?.let { stack ->
            stack.forEachIndexed { i, card ->
                drawCard(canvas, card, dragX - calculatedCardWidth / 2, dragY - calculatedCardHeight / 2 + i * (calculatedCardHeight * viewModel.tableauCardRevealFactor))
            }
        }

        // Draw any cards being animated automatically
        for (anim in activeAnimations) {
            val t = anim.animator.animatedValue as Float
            val currentX = anim.startX + (anim.endX - anim.startX) * t
            val currentY = anim.startY + (anim.endY - anim.startY) * t
            drawCard(canvas, anim.card, currentX, currentY)
        }

        if (viewModel.checkWin()) {
            paint.color = Color.YELLOW
            paint.textSize = 80f
            canvas.drawText("🎉 You Win!", width / 3f, height / 2f, paint)
        }
    }

    private fun drawStockAndWaste(canvas: Canvas, animatingCards: List<Card>) {
        drawPile(canvas, viewModel.stock.first(), getPileX(viewModel.stock.first()), animatingCards)
        drawPile(canvas, viewModel.waste.first(), getPileX(viewModel.waste.first()), animatingCards)
    }

    private fun drawFreeCells(canvas: Canvas, animatingCards: List<Card>) {
        viewModel.freeCells.forEach { pile ->
            drawPile(canvas, pile, getPileX(pile), animatingCards)
        }
    }


    private fun drawFoundations(canvas: Canvas, animatingCards: List<Card>) {
        viewModel.foundations.forEach { pile ->
            drawPile(canvas, pile, getPileX(pile), animatingCards)
        }
    }

    private fun drawTableau(canvas: Canvas, animatingCards: List<Card>) {
        viewModel.tableau.forEach { pile ->
            val x = getPileX(pile)
            pile.cards.forEachIndexed { j, card ->
                val y = getPileY(pile) + j * (calculatedCardHeight * viewModel.tableauCardRevealFactor)
                if (dragStack?.contains(card) != true && card !in animatingCards) {
                    drawCard(canvas, card, x, y)
                }
                card.x = x
                card.y = y
            }
        }
    }

    private fun drawPile(canvas: Canvas, pile: Pile, x: Float, animatingCards: List<Card>) {
        val y = getPileY(pile)
        val border = Paint().apply {
            color = Color.argb(100, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawRect(x - calculatedCardWidth / 2, y - calculatedCardHeight / 2, x + calculatedCardWidth / 2, y + calculatedCardHeight / 2, border)

        when (pile.type) {
            PileType.WASTE -> {
                val cardsToDraw = if (previousWasteState.isNotEmpty()) {
                    previousWasteState.takeLast(3)
                } else {
                    pile.cards.takeLast(3)
                }
                cardsToDraw.forEachIndexed { i, card ->
                    if (dragStack?.contains(card) != true && card !in animatingCards) {
                        val cardX = x + i * (calculatedCardWidth * WASTE_CARD_REVEAL_FACTOR)
                        drawCard(canvas, card, cardX, y)
                        card.x = cardX
                        card.y = y
                    }
                }
            }

            else -> {
                val topCard = pile.topCard()
                if (topCard != null) {
                    val isDraggingTopCard = dragStack?.contains(topCard) == true
                    if (isDraggingTopCard && pile.cards.size > 1) {
                        val secondCard = pile.cards[pile.cards.size - 2]
                        if (secondCard !in animatingCards) {
                            drawCard(canvas, secondCard, x, y)
                            secondCard.x = x
                            secondCard.y = y
                        }
                    } else if (!isDraggingTopCard && topCard !in animatingCards) {
                        drawCard(canvas, topCard, x, y)
                        topCard.x = x
                        topCard.y = y
                    }
                }
            }
        }
    }

    private fun drawCard(canvas: Canvas, card: Card, x: Float, y: Float) {
        val left = x - calculatedCardWidth / 2f
        val top = y - calculatedCardHeight / 2f
        val rect = RectF(left, top, left + calculatedCardWidth, top + calculatedCardHeight)

        if (card.faceUp) {
            val image = cardImages[card.imageName]
            image?.let { canvas.drawBitmap(it, left, top, paint) }
        } else {
            cardBackImage?.let {
                canvas.drawBitmap(it, left, top, paint)
            } ?: run {
                val bgPaint = Paint().apply {
                    color = Color.DKGRAY
                    style = Paint.Style.FILL
                }
                canvas.drawRoundRect(rect, 12f, 12f, bgPaint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isAutoCompleting || activeAnimations.isNotEmpty()) return true

        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = x
                downY = y
                isDragging = false
                potentialDragPile = null
                potentialDragStack = null

                if (viewModel.gameType == GameType.KLONDIKE) {
                    // Tap stock
                    val stockPile = viewModel.stock.first()
                    val stockX = getPileX(stockPile)
                    val stockY = getPileY(stockPile)
                    val stockRect = RectF(stockX - calculatedCardWidth / 2, stockY - calculatedCardHeight / 2, stockX + calculatedCardWidth / 2, stockY + calculatedCardHeight / 2)
                    if (stockRect.contains(x, y)) {
                        performClick()
                        previousWasteState = viewModel.waste.first().cards.toList()
                        val drawnCards = viewModel.drawFromStock()
                        if (drawnCards.isNotEmpty()) {
                            val wasteX = getPileX(viewModel.waste.first())
                            val pileY = getPileY(viewModel.waste.first())
                            drawnCards.forEachIndexed { i, card ->
                                card.x = stockX
                                card.y = pileY
                                animateCardTo(card, wasteX + i * 40f, pileY) {
                                    if (i == drawnCards.size - 1) {
                                        previousWasteState = emptyList()
                                        invalidate()
                                    }
                                }
                            }
                        } else {
                            invalidate() // Recycled
                        }
                        return true
                    }
                }


                // Check for card tap/drag on tableau, waste, foundations or freecells
                val pileGroups = mutableListOf<List<Pile>>()
                pileGroups.add(viewModel.tableau.reversed())
                if (viewModel.gameType == GameType.KLONDIKE) {
                    pileGroups.add(viewModel.waste)
                }
                pileGroups.add(viewModel.foundations.reversed())
                pileGroups.add(viewModel.freeCells.reversed())


                for (pileGroup in pileGroups) {
                    for (pile in pileGroup) {
                        val cards = pile.cards
                        if (pile.type == PileType.TABLEAU) {
                            // More precise hit detection for stacked tableau cards
                            val pileX = getPileX(pile)
                            val pileLeft = pileX - calculatedCardWidth / 2f
                            val pileRight = pileX + calculatedCardWidth / 2f

                            if (x in pileLeft..pileRight) { // Check if touch is within the pile's width
                                for (i in cards.indices.reversed()) {
                                    val card = cards[i]
                                    if (card.faceUp) {
                                        val cardTop = card.y - calculatedCardHeight / 2f
                                        val cardBottom = card.y + calculatedCardHeight / 2f

                                        val isTopCard = (i == cards.size - 1)
                                        val tappableBottom = if (isTopCard) cardBottom else {
                                            val nextCardTop = cards[i + 1].y - calculatedCardHeight / 2f
                                            nextCardTop
                                        }

                                        if (y in cardTop..tappableBottom) {
                                            potentialDragPile = pile
                                            potentialDragStack = cards.subList(i, cards.size).toMutableList()
                                            return true
                                        }
                                    }
                                }
                            }
                        } else {
                            // For waste and foundations, only the top card is interactive
                            val topCard = pile.topCard()
                            if (topCard != null) {
                                val cardRect = RectF(topCard.x - calculatedCardWidth / 2, topCard.y - calculatedCardHeight / 2, topCard.x + calculatedCardWidth / 2, topCard.y + calculatedCardHeight / 2)
                                if (cardRect.contains(x,y)) {
                                    potentialDragPile = pile
                                    potentialDragStack = mutableListOf(topCard)
                                    return true
                                }
                            }
                        }
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (potentialDragStack != null && !isDragging) {
                    if (abs(x - downX) > touchSlop || abs(y - downY) > touchSlop) {
                        isDragging = true
                        dragStack = potentialDragStack
                        dragFromPile = potentialDragPile
                        potentialDragStack = null
                        potentialDragPile = null
                        dragX = x
                        dragY = y
                        invalidate()
                    }
                } else if (isDragging) {
                    dragX = x
                    dragY = y
                    invalidate()
                }
            }

            MotionEvent.ACTION_UP -> {
                if (isDragging) {
                    // Handle drop
                    dragStack?.let { stack ->
                        dragFromPile?.let { fromPile ->
                            // Try foundations (only single cards)
                            if (stack.size == 1) {
                                for (foundation in viewModel.foundations) {
                                    val fx = getPileX(foundation)
                                    val fy = getPileY(foundation)
                                    if (x in fx..(fx + calculatedCardWidth) && y in fy..(fy + calculatedCardHeight)) {
                                        if (viewModel.canPlaceOnFoundation(stack.first(), foundation)) {
                                            viewModel.moveToFoundation(fromPile, foundation)
                                            resetDrag()
                                            invalidate()
                                            checkForAutoComplete()
                                            return true
                                        }
                                    }
                                }
                                // Try freecells (only single cards)
                                for (freeCell in viewModel.freeCells) {
                                    val fcx = getPileX(freeCell)
                                    val fcy = getPileY(freeCell)
                                    if (x in fcx..(fcx + calculatedCardWidth) && y in fcy..(fcy + calculatedCardHeight)) {
                                        viewModel.moveStackToFreeCell(fromPile, stack, freeCell)
                                        resetDrag()
                                        invalidate()
                                        checkForAutoComplete()
                                        return true
                                    }
                                }
                            }

                            // Try tableau
                            for (pile in viewModel.tableau) {
                                val px = getPileX(pile)
                                val tableauStartY = getPileY(pile) - calculatedCardHeight / 2f
                                if (x in px..(px + calculatedCardWidth) && y > tableauStartY) {
                                    if (viewModel.canPlaceOnTableau(stack, pile)) {
                                        viewModel.moveStackToTableau(fromPile, stack, pile)
                                        resetDrag()
                                        invalidate()
                                        checkForAutoComplete()
                                        return true
                                    }
                                }
                            }
                        }
                        // Invalid move, snap back
                        resetDrag()
                        invalidate()
                        return true
                    }
                } else {
                    // Handle tap for auto-move
                    potentialDragStack?.let { stack ->
                        potentialDragPile?.let { pile ->
                            val card = stack.first()
                            if (card == pile.topCard()) { // Can only auto-move top card
                                val targetPile = viewModel.autoMoveCard(card, pile)
                                if (targetPile != null) {
                                    performClick()
                                    val targetX = getPileX(targetPile)
                                    val targetY = getPileY(targetPile)
                                    animateCardTo(card, targetX, targetY) {
                                        invalidate()
                                        checkForAutoComplete()
                                    }
                                }
                            }
                        }
                    }
                }
                resetDrag()
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun getPileX(pile: Pile): Float {
        val leftMarginPx = dpToPx(viewModel.leftMargin)
        val rightMarginPx = dpToPx(viewModel.rightMargin)
        val topLeftX =  when (pile.type) {
            PileType.STOCK -> leftMarginPx
            PileType.WASTE -> leftMarginPx + calculatedCardWidth + INTER_CARD_SPACING
            PileType.FOUNDATION -> {
                if (viewModel.gameType == GameType.FREECELL) {
                    val tableauIndex = 4 + viewModel.foundations.indexOf(pile)
                    tableauStartX + tableauIndex * (calculatedCardWidth + INTER_CARD_SPACING)
                } else {
                    val foundationCount = viewModel.foundations.size
                    val startX = width - (foundationCount * (calculatedCardWidth + INTER_CARD_SPACING)) - rightMarginPx
                    startX + viewModel.foundations.indexOf(pile) * (calculatedCardWidth + INTER_CARD_SPACING)
                }
            }

            PileType.TABLEAU -> tableauStartX + viewModel.tableau.indexOf(pile) * (calculatedCardWidth + INTER_CARD_SPACING)
            PileType.FREE_CELL -> leftMarginPx + viewModel.freeCells.indexOf(pile) * (calculatedCardWidth + INTER_CARD_SPACING)
        }
        return topLeftX + calculatedCardWidth / 2f
    }

    private fun getPileY(pile: Pile): Float {
        val topMargin = if (isLandscape) {
            VERTICAL_MARGIN
        } else {
            height * 0.4f
        }
        return when (pile.type) {
            PileType.TABLEAU -> topMargin + calculatedCardHeight + INTER_ROW_SPACING + calculatedCardHeight / 2f
            else -> topMargin + calculatedCardHeight / 2f
        }
    }

    private fun animateCardTo(card: Card, targetX: Float, targetY: Float, onEnd: () -> Unit = {}) {
        val startX = card.x
        val startY = card.y
        val anim = ValueAnimator.ofFloat(0f, 1f)
        anim.duration = 200

        val animationState = AnimationState(card, startX, startY, targetX, targetY, anim)

        anim.addUpdateListener { invalidate() }
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                activeAnimations.add(animationState)
            }

            override fun onAnimationEnd(animation: Animator) {
                card.x = targetX
                card.y = targetY
                activeAnimations.remove(animationState)
                onEnd()
            }
        })
        anim.start()
    }

    private fun resetDrag() {
        dragStack = null
        dragFromPile = null
        potentialDragStack = null
        potentialDragPile = null
        isDragging = false
        dragX = 0f
        dragY = 0f
    }

    private fun runAutoCompleteStep() {
        if (!isAutoCompleting) return

        val result = viewModel.autoMoveToFoundation()
        if (result != null) {
            val (card, pile) = result
            val targetX = getPileX(pile)
            val targetY = getPileY(pile)
            animateCardTo(card, targetX, targetY) {
                runAutoCompleteStep()
            }
        } else {
            isAutoCompleting = false
            invalidate()
        }
    }

    fun startAutoCompleteAnimation() {
        if (isAutoCompleting) return
        isAutoCompleting = true
        runAutoCompleteStep()
    }

    private fun checkForAutoComplete() {
        if (viewModel.isGameWinnable()) {
            startAutoCompleteAnimation()
        }
    }
}
