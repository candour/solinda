package com.example.solinda.jewelinda.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.solinda.jewelinda.JewelindaViewModel

@Composable
fun JewelindaScreen(viewModel: JewelindaViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        GameGrid(viewModel = viewModel)
    }
}
