package com.example.solinda.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.solinda.GameViewModel
import com.example.solinda.GameType
import com.example.solinda.GameRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: GameViewModel,
    repository: GameRepository,
    onClose: () -> Unit
) {
    var selectedGameType by remember { mutableStateOf(viewModel.gameType) }
    var selectedDealCount by remember { mutableStateOf(viewModel.dealCount) }
    var leftMargin by remember { mutableFloatStateOf(viewModel.leftMargin.toFloat()) }
    var rightMargin by remember { mutableFloatStateOf(viewModel.rightMargin.toFloat()) }
    var leftMarginLandscape by remember { mutableFloatStateOf(viewModel.leftMarginLandscape.toFloat()) }
    var rightMarginLandscape by remember { mutableFloatStateOf(viewModel.rightMarginLandscape.toFloat()) }
    var revealFactor by remember { mutableFloatStateOf(viewModel.tableauCardRevealFactor) }
    var hapticsEnabled by remember { mutableStateOf(viewModel.isHapticsEnabled) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                actions = {
                    Button(onClick = {
                        viewModel.leftMargin = leftMargin.toInt()
                        viewModel.rightMargin = rightMargin.toInt()
                        viewModel.leftMarginLandscape = leftMarginLandscape.toInt()
                        viewModel.rightMarginLandscape = rightMarginLandscape.toInt()
                        viewModel.tableauCardRevealFactor = revealFactor
                        viewModel.isHapticsEnabled = hapticsEnabled
                        viewModel.resetGame(selectedGameType, selectedDealCount)
                        viewModel.saveGame(repository)
                        onClose()
                    }) {
                        Text("Save")
                    }
                }
            )
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            val isLandscape = maxWidth > maxHeight
            if (isLandscape) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Column 1: Game Type and Deal Count
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Game Type", style = MaterialTheme.typography.titleMedium)
                        GameTypeSelector(selectedGameType, onSelect = { selectedGameType = it })

                        if (selectedGameType == GameType.KLONDIKE) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Deal Count", style = MaterialTheme.typography.titleMedium)
                            DealCountSelector(selectedDealCount, onSelect = { selectedDealCount = it })
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = hapticsEnabled, onCheckedChange = { hapticsEnabled = it })
                            Text("Haptic Feedback")
                        }
                    }

                    Spacer(modifier = Modifier.width(32.dp))

                    // Column 2: Margins and Reveal Factor
                    Column(modifier = Modifier.weight(1f)) {
                        if (selectedGameType == GameType.KLONDIKE || selectedGameType == GameType.FREECELL) {
                            Text("Left Margin (Portrait): ${leftMargin.toInt()}", style = MaterialTheme.typography.bodyMedium)
                            Slider(value = leftMargin, onValueChange = { leftMargin = it }, valueRange = 0f..200f)
                            Text("Right Margin (Portrait): ${rightMargin.toInt()}", style = MaterialTheme.typography.bodyMedium)
                            Slider(value = rightMargin, onValueChange = { rightMargin = it }, valueRange = 0f..200f)
                            Text("Left Margin (Landscape): ${leftMarginLandscape.toInt()}", style = MaterialTheme.typography.bodyMedium)
                            Slider(value = leftMarginLandscape, onValueChange = { leftMarginLandscape = it }, valueRange = 0f..200f)
                            Text("Right Margin (Landscape): ${rightMarginLandscape.toInt()}", style = MaterialTheme.typography.bodyMedium)
                            Slider(value = rightMarginLandscape, onValueChange = { rightMarginLandscape = it }, valueRange = 0f..200f)
                            Text("Tableau Reveal Factor: ${String.format("%.2f", revealFactor)}", style = MaterialTheme.typography.bodyMedium)
                            Slider(value = revealFactor, onValueChange = { revealFactor = it }, valueRange = 0f..0.5f)
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Game Type", style = MaterialTheme.typography.titleMedium)
                    GameTypeSelector(selectedGameType, onSelect = { selectedGameType = it })

                    if (selectedGameType == GameType.KLONDIKE) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Deal Count", style = MaterialTheme.typography.titleMedium)
                        DealCountSelector(selectedDealCount, onSelect = { selectedDealCount = it })
                    }

                    if (selectedGameType == GameType.KLONDIKE || selectedGameType == GameType.FREECELL) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Left Margin (Portrait): ${leftMargin.toInt()}", style = MaterialTheme.typography.bodyMedium)
                        Slider(value = leftMargin, onValueChange = { leftMargin = it }, valueRange = 0f..200f)
                        Text("Right Margin (Portrait): ${rightMargin.toInt()}", style = MaterialTheme.typography.bodyMedium)
                        Slider(value = rightMargin, onValueChange = { rightMargin = it }, valueRange = 0f..200f)
                        Text("Left Margin (Landscape): ${leftMarginLandscape.toInt()}", style = MaterialTheme.typography.bodyMedium)
                        Slider(value = leftMarginLandscape, onValueChange = { leftMarginLandscape = it }, valueRange = 0f..200f)
                        Text("Right Margin (Landscape): ${rightMarginLandscape.toInt()}", style = MaterialTheme.typography.bodyMedium)
                        Slider(value = rightMarginLandscape, onValueChange = { rightMarginLandscape = it }, valueRange = 0f..200f)
                        Text("Tableau Reveal Factor: ${String.format("%.2f", revealFactor)}", style = MaterialTheme.typography.bodyMedium)
                        Slider(value = revealFactor, onValueChange = { revealFactor = it }, valueRange = 0f..0.5f)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = hapticsEnabled, onCheckedChange = { hapticsEnabled = it })
                        Text("Haptic Feedback")
                    }
                }
            }
        }
    }
}

@Composable
fun GameTypeSelector(selected: GameType, onSelect: (GameType) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        GameType.entries.forEach { gameType ->
            val icon = when (gameType) {
                GameType.KLONDIKE -> GameTypeIcons.Klondike
                GameType.FREECELL -> GameTypeIcons.FreeCell
                GameType.JEWELINDA -> GameTypeIcons.Jewelinda
                GameType.COMPASS -> GameTypeIcons.Compass
                GameType.CALCULATOR -> GameTypeIcons.Calculator
            }
            val isSelected = gameType == selected
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .selectable(
                        selected = isSelected,
                        onClick = { onSelect(gameType) },
                        role = Role.RadioButton
                    )
                    .background(
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = gameType.name,
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun DealCountSelector(selected: Int, onSelect: (Int) -> Unit) {
    Row(Modifier.selectableGroup()) {
        listOf(1, 3).forEach { count ->
            Row(
                Modifier
                    .selectable(
                        selected = (count == selected),
                        onClick = { onSelect(count) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = (count == selected), onClick = null)
                Text(text = "Deal $count", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}
