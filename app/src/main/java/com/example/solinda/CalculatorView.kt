package com.example.solinda

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View

class CalculatorView(context: Context, private val viewModel: GameViewModel) : View(context) {

    private val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.BLACK
        strokeWidth = 3f
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 60f
        textAlign = Paint.Align.CENTER
    }

    private var cardWidth = 0f
    private var cardHeight = 0f
    private val buttonGrid = mutableMapOf<String, RectF>()

    private val buttonLabels = arrayOf(
        arrayOf("AC", "C", "%", "/"),
        arrayOf("7", "8", "9", "*"),
        arrayOf("4", "5", "6", "-"),
        arrayOf("1", "2", "3", "+"),
        arrayOf("0", ".", "√", "="),
        arrayOf("MC", "MR", "M-", "M+")
    )

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cardWidth = w / 5f
        cardHeight = cardWidth * 1.4f
        calculateButtonLayout(w.toFloat())
    }

    private fun calculateButtonLayout(width: Float) {
        buttonGrid.clear()
        val buttonWidth = width / 4f
        val buttonHeight = buttonWidth * 1.2f
        val displayHeight = cardHeight + 40f

        for (rowIndex in buttonLabels.indices) {
            for (colIndex in buttonLabels[rowIndex].indices) {
                val label = buttonLabels[rowIndex][colIndex]
                val left = colIndex * buttonWidth
                val top = displayHeight + (rowIndex * buttonHeight)
                val rect = RectF(left, top, left + buttonWidth, top + buttonHeight)
                buttonGrid[label] = rect
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawDisplay(canvas)
        drawButtons(canvas)
    }

    private fun drawDisplay(canvas: Canvas) {
        val displayValue = viewModel.calculatorState.display
        val isNegative = displayValue.startsWith("-")
        val numberString = if (isNegative) displayValue.substring(1) else displayValue

        val startX = 20f
        val y = 20f

        for ((index, char) in numberString.withIndex()) {
            val x = startX + (index * cardWidth * 0.6f)
            val rect = RectF(x, y, x + cardWidth, y + cardHeight)
            // In a real implementation, we would draw card bitmaps here.
            // For now, we'll draw placeholders.
            val cardPaint = Paint().apply { color = if (isNegative) Color.RED else Color.BLACK }
            canvas.drawRect(rect, cardPaint)
            canvas.drawText(char.toString(), rect.centerX(), rect.centerY() + 20, textPaint.apply{ color = Color.WHITE})
        }
    }

    private fun drawButtons(canvas: Canvas) {
        textPaint.color = Color.BLACK
        for ((label, rect) in buttonGrid) {
            canvas.drawRoundRect(rect, 10f, 10f, buttonPaint)
            canvas.drawText(label, rect.centerX(), rect.centerY() + 20, textPaint)
        }
    }

    override fun onTouchEvent(event: android.view.MotionEvent): Boolean {
        if (event.action == android.view.MotionEvent.ACTION_DOWN) {
            for ((label, rect) in buttonGrid) {
                if (rect.contains(event.x, event.y)) {
                    viewModel.onCalculatorInput(label)
                    invalidate() // Redraw the view
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }
}
