package com.example.solinda.jewelinda.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.solinda.jewelinda.JewelindaViewModel

@Composable
fun JewelindaScreen(viewModel: JewelindaViewModel) {
    val score by viewModel.score.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Score: $score",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
        GameGrid(viewModel = viewModel)
    }
}
