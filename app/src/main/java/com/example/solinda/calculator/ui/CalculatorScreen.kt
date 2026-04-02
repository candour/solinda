package com.example.solinda.calculator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.solinda.R
import com.example.solinda.calculator.CalculatorViewModel

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    onOptionsClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onOptionsClick,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(stringResource(R.string.options))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (viewModel.memoryDisplayText.isNotEmpty()) {
                Text(
                    text = viewModel.memoryDisplayText,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                    fontSize = 24.sp,
                    color = Color.LightGray,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1
                )
            }

            Text(
                text = viewModel.displayText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp, top = 8.dp),
                textAlign = TextAlign.End,
                fontSize = 64.sp,
                color = Color.White,
                fontWeight = FontWeight.Light,
                maxLines = 1
            )

            // Memory Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalculatorButton(stringResource(R.string.mc), Color.DarkGray, Modifier.weight(1f)) { viewModel.onMemoryClear() }
                CalculatorButton(stringResource(R.string.mr), Color.DarkGray, Modifier.weight(1f)) { viewModel.onMemoryRecall() }
                CalculatorButton(stringResource(R.string.m_plus), Color.DarkGray, Modifier.weight(1f)) { viewModel.onMemoryAdd() }
                CalculatorButton(stringResource(R.string.m_minus), Color.DarkGray, Modifier.weight(1f)) { viewModel.onMemorySubtract() }
                CalculatorButton(stringResource(R.string.backspace), Color.DarkGray, Modifier.weight(1f)) { viewModel.onBackspaceClick() }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalculatorButton(stringResource(R.string.ac), Color.LightGray, Modifier.weight(1f), Color.Black) { viewModel.onACClick() }
                CalculatorButton(stringResource(R.string.plus_minus), Color.LightGray, Modifier.weight(1f), Color.Black) { viewModel.onPlusMinusClick() }
                CalculatorButton(stringResource(R.string.percentage), Color.LightGray, Modifier.weight(1f), Color.Black) { viewModel.onPercentageClick() }
                CalculatorButton(
                    stringResource(R.string.divide),
                    Color(0xFFFFA500),
                    Modifier.weight(1f),
                    isHighlighted = viewModel.pendingOperator == "/"
                ) { viewModel.onOperatorClick("/") }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalculatorButton("7", Color(0xFF333333), Modifier.weight(1f)) { viewModel.onNumberClick("7") }
                CalculatorButton("8", Color(0xFF333333), Modifier.weight(1f)) { viewModel.onNumberClick("8") }
                CalculatorButton("9", Color(0xFF333333), Modifier.weight(1f)) { viewModel.onNumberClick("9") }
                CalculatorButton(
                    stringResource(R.string.multiply),
                    Color(0xFFFFA500),
                    Modifier.weight(1f),
                    isHighlighted = viewModel.pendingOperator == "*"
                ) { viewModel.onOperatorClick("*") }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 3
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalculatorButton("4", Color(0xFF333333), Modifier.weight(1f)) { viewModel.onNumberClick("4") }
                CalculatorButton("5", Color(0xFF333333), Modifier.weight(1f)) { viewModel.onNumberClick("5") }
                CalculatorButton("6", Color(0xFF333333), Modifier.weight(1f)) { viewModel.onNumberClick("6") }
                CalculatorButton(
                    stringResource(R.string.subtract),
                    Color(0xFFFFA500),
                    Modifier.weight(1f),
                    isHighlighted = viewModel.pendingOperator == "-"
                ) { viewModel.onOperatorClick("-") }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 4
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalculatorButton("1", Color(0xFF333333), Modifier.weight(1f)) { viewModel.onNumberClick("1") }
                CalculatorButton("2", Color(0xFF333333), Modifier.weight(1f)) { viewModel.onNumberClick("2") }
                CalculatorButton("3", Color(0xFF333333), Modifier.weight(1f)) { viewModel.onNumberClick("3") }
                CalculatorButton(
                    stringResource(R.string.add),
                    Color(0xFFFFA500),
                    Modifier.weight(1f),
                    isHighlighted = viewModel.pendingOperator == "+"
                ) { viewModel.onOperatorClick("+") }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 5
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalculatorButton("0", Color(0xFF333333), Modifier.weight(2f)) { viewModel.onNumberClick("0") }
                CalculatorButton(stringResource(R.string.dot), Color(0xFF333333), Modifier.weight(1f)) { viewModel.onDecimalClick() }
                CalculatorButton(stringResource(R.string.equals), Color(0xFFFFA500), Modifier.weight(1f)) { viewModel.onEqualsClick() }
            }
        }
    }
}

@Composable
fun CalculatorButton(
    text: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    isHighlighted: Boolean = false,
    onClick: () -> Unit
) {
    val finalBackgroundColor = if (isHighlighted) Color(0xFFFFC0CB) else backgroundColor
    val finalTextColor = if (isHighlighted) Color.Black else textColor

    Box(
        modifier = modifier
            .aspectRatio(if (text == "0") 2f else 1f)
            .clip(CircleShape)
            .background(finalBackgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = finalTextColor,
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
