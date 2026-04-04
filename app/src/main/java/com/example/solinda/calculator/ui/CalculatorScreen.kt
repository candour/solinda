package com.example.solinda.calculator.ui

import android.content.res.Configuration
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

import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import com.example.solinda.GameViewModel

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    gameViewModel: GameViewModel,
    onOptionsClick: () -> Unit
) {
    val view = LocalView.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val onButtonClick: (() -> Unit) -> Unit = { action ->
        action()
        if (gameViewModel.isHapticsEnabled) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        val buttonSpacing = 8.dp

        val displaySection = @Composable {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Bottom
            ) {
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
                        .padding(bottom = 16.dp, top = 8.dp),
                    textAlign = TextAlign.End,
                    fontSize = if (isLandscape) 48.sp else 64.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Light,
                    maxLines = 1
                )
            }
        }

        val memoryRow = @Composable {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
            ) {
                CalculatorButton(stringResource(R.string.mc), Color.DarkGray, Modifier.weight(1f)) { onButtonClick { viewModel.onMemoryClear() } }
                CalculatorButton(stringResource(R.string.mr), Color.DarkGray, Modifier.weight(1f)) { onButtonClick { viewModel.onMemoryRecall() } }
                CalculatorButton(stringResource(R.string.m_plus), Color.DarkGray, Modifier.weight(1f)) { onButtonClick { viewModel.onMemoryAdd() } }
                CalculatorButton(stringResource(R.string.m_minus), Color.DarkGray, Modifier.weight(1f)) { onButtonClick { viewModel.onMemorySubtract() } }
                CalculatorButton(stringResource(R.string.backspace), Color.DarkGray, Modifier.weight(1f)) { onButtonClick { viewModel.onBackspaceClick() } }
            }
        }

        val row1 = @Composable {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
            ) {
                CalculatorButton(stringResource(R.string.ac), Color.LightGray, Modifier.weight(1f), Color.Black) { onButtonClick { viewModel.onACClick() } }
                CalculatorButton(stringResource(R.string.plus_minus), Color.LightGray, Modifier.weight(1f), Color.Black) { onButtonClick { viewModel.onPlusMinusClick() } }
                CalculatorButton(stringResource(R.string.percentage), Color.LightGray, Modifier.weight(1f), Color.Black) { onButtonClick { viewModel.onPercentageClick() } }
                CalculatorButton(
                    stringResource(R.string.divide),
                    Color(0xFFFFA500),
                    Modifier.weight(1f),
                    isHighlighted = viewModel.pendingOperator == "/"
                ) { onButtonClick { viewModel.onOperatorClick("/") } }
            }
        }

        val row2 = @Composable {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
            ) {
                CalculatorButton("7", Color(0xFF333333), Modifier.weight(1f)) { onButtonClick { viewModel.onNumberClick("7") } }
                CalculatorButton("8", Color(0xFF333333), Modifier.weight(1f)) { onButtonClick { viewModel.onNumberClick("8") } }
                CalculatorButton("9", Color(0xFF333333), Modifier.weight(1f)) { onButtonClick { viewModel.onNumberClick("9") } }
                CalculatorButton(
                    stringResource(R.string.multiply),
                    Color(0xFFFFA500),
                    Modifier.weight(1f),
                    isHighlighted = viewModel.pendingOperator == "*"
                ) { onButtonClick { viewModel.onOperatorClick("*") } }
            }
        }

        val row3 = @Composable {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
            ) {
                CalculatorButton("4", Color(0xFF333333), Modifier.weight(1f)) { onButtonClick { viewModel.onNumberClick("4") } }
                CalculatorButton("5", Color(0xFF333333), Modifier.weight(1f)) { onButtonClick { viewModel.onNumberClick("5") } }
                CalculatorButton("6", Color(0xFF333333), Modifier.weight(1f)) { onButtonClick { viewModel.onNumberClick("6") } }
                CalculatorButton(
                    stringResource(R.string.subtract),
                    Color(0xFFFFA500),
                    Modifier.weight(1f),
                    isHighlighted = viewModel.pendingOperator == "-"
                ) { onButtonClick { viewModel.onOperatorClick("-") } }
            }
        }

        val row4 = @Composable {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
            ) {
                CalculatorButton("1", Color(0xFF333333), Modifier.weight(1f)) { onButtonClick { viewModel.onNumberClick("1") } }
                CalculatorButton("2", Color(0xFF333333), Modifier.weight(1f)) { onButtonClick { viewModel.onNumberClick("2") } }
                CalculatorButton("3", Color(0xFF333333), Modifier.weight(1f)) { onButtonClick { viewModel.onNumberClick("3") } }
                CalculatorButton(
                    stringResource(R.string.add),
                    Color(0xFFFFA500),
                    Modifier.weight(1f),
                    isHighlighted = viewModel.pendingOperator == "+"
                ) { onButtonClick { viewModel.onOperatorClick("+") } }
            }
        }

        val row5 = @Composable {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
            ) {
                CalculatorButton("0", Color(0xFF333333), Modifier.weight(2f)) { onButtonClick { viewModel.onNumberClick("0") } }
                CalculatorButton(stringResource(R.string.dot), Color(0xFF333333), Modifier.weight(1f)) { onButtonClick { viewModel.onDecimalClick() } }
                CalculatorButton(stringResource(R.string.equals), Color(0xFFFFA500), Modifier.weight(1f)) { onButtonClick { viewModel.onEqualsClick() } }
            }
        }

        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .statusBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = onOptionsClick,
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Text(stringResource(R.string.options))
                        }
                    }
                    displaySection()
                    memoryRow()
                    row1()
                }

                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    row2()
                    row3()
                    row4()
                    row5()
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .statusBarsPadding()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = onOptionsClick,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(stringResource(R.string.options))
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    displaySection()
                    Spacer(modifier = Modifier.height(buttonSpacing))
                    memoryRow()
                    Spacer(modifier = Modifier.height(buttonSpacing))
                    row1()
                    Spacer(modifier = Modifier.height(buttonSpacing))
                    row2()
                    Spacer(modifier = Modifier.height(buttonSpacing))
                    row3()
                    Spacer(modifier = Modifier.height(buttonSpacing))
                    row4()
                    Spacer(modifier = Modifier.height(buttonSpacing))
                    row5()
                }
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
            .aspectRatio(if (text == "0") 2.1f else 1f) // Slightly wider 0 to prevent overlap
            .clip(CircleShape)
            .background(finalBackgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = finalTextColor,
            fontSize = 24.sp, // Reduced font size slightly
            fontWeight = FontWeight.Medium
        )
    }
}
