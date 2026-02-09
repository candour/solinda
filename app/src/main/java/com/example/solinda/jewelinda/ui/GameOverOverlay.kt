package com.example.solinda.jewelinda.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.solinda.jewelinda.JewelindaViewModel

@Composable
fun GameOverOverlay(
    score: Int,
    targetScore: Int,
    onPlayAgain: () -> Unit
) {
    val isWin = score >= targetScore

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isWin) "🎉 YOU WIN!" else "GAME OVER",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isWin) Color(0xFF4CAF50) else Color.Red
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Final Score: $score",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Target Score: $targetScore",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = onPlayAgain) {
                Text(text = "Play Again", fontSize = 18.sp)
            }
        }
    }
}
