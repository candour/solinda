package com.example.solinda.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    var leftMargin by remember { mutableStateOf(viewModel.leftMargin.toString()) }
    var rightMargin by remember { mutableStateOf(viewModel.rightMargin.toString()) }
    var leftMarginLandscape by remember { mutableStateOf(viewModel.leftMarginLandscape.toString()) }
    var rightMarginLandscape by remember { mutableStateOf(viewModel.rightMarginLandscape.toString()) }
    var revealFactor by remember { mutableStateOf(viewModel.tableauCardRevealFactor.toString()) }
    var hapticsEnabled by remember { mutableStateOf(viewModel.isHapticsEnabled) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                actions = {
                    Button(onClick = {
                        viewModel.gameType = selectedGameType
                        viewModel.dealCount = selectedDealCount
                        viewModel.leftMargin = leftMargin.toIntOrNull() ?: viewModel.leftMargin
                        viewModel.rightMargin = rightMargin.toIntOrNull() ?: viewModel.rightMargin
                        viewModel.leftMarginLandscape = leftMarginLandscape.toIntOrNull() ?: viewModel.leftMarginLandscape
                        viewModel.rightMarginLandscape = rightMarginLandscape.toIntOrNull() ?: viewModel.rightMarginLandscape
                        viewModel.tableauCardRevealFactor = revealFactor.toFloatOrNull() ?: viewModel.tableauCardRevealFactor
                        viewModel.isHapticsEnabled = hapticsEnabled
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

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Deal Count", style = MaterialTheme.typography.titleMedium)
                        DealCountSelector(selectedDealCount, onSelect = { selectedDealCount = it })
                    }

                    Spacer(modifier = Modifier.width(32.dp))

                    // Column 2: Margins, Reveal Factor, and Haptics
                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(value = leftMargin, onValueChange = { leftMargin = it }, label = { Text("Left Margin (Portrait)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = rightMargin, onValueChange = { rightMargin = it }, label = { Text("Right Margin (Portrait)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = leftMarginLandscape, onValueChange = { leftMarginLandscape = it }, label = { Text("Left Margin (Landscape)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = rightMarginLandscape, onValueChange = { rightMarginLandscape = it }, label = { Text("Right Margin (Landscape)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = revealFactor, onValueChange = { revealFactor = it }, label = { Text("Tableau Reveal Factor") }, modifier = Modifier.fillMaxWidth())

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = hapticsEnabled, onCheckedChange = { hapticsEnabled = it })
                            Text("Haptic Feedback")
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

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Deal Count", style = MaterialTheme.typography.titleMedium)
                    DealCountSelector(selectedDealCount, onSelect = { selectedDealCount = it })

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(value = leftMargin, onValueChange = { leftMargin = it }, label = { Text("Left Margin (Portrait)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = rightMargin, onValueChange = { rightMargin = it }, label = { Text("Right Margin (Portrait)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = leftMarginLandscape, onValueChange = { leftMarginLandscape = it }, label = { Text("Left Margin (Landscape)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = rightMarginLandscape, onValueChange = { rightMarginLandscape = it }, label = { Text("Right Margin (Landscape)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = revealFactor, onValueChange = { revealFactor = it }, label = { Text("Tableau Reveal Factor") }, modifier = Modifier.fillMaxWidth())

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
    Column(Modifier.selectableGroup()) {
        GameType.entries.forEach { gameType ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .selectable(
                        selected = (gameType == selected),
                        onClick = { onSelect(gameType) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = (gameType == selected), onClick = null)
                Text(
                    text = gameType.name.lowercase().replaceFirstChar { it.uppercase() },
                    modifier = Modifier.padding(start = 16.dp)
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
