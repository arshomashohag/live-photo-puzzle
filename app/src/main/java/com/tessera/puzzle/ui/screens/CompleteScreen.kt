package com.tessera.puzzle.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tessera.puzzle.game.GameViewModel
import com.tessera.puzzle.ui.theme.BlueprintButton
import com.tessera.puzzle.ui.theme.TesseraColors
import com.tessera.puzzle.ui.theme.TesseraType

private fun fmt(ms: Long): String {
    val s = ms / 1000
    return "%02d:%02d".format(s / 60, s % 60)
}

@Composable
fun CompleteScreen(game: GameViewModel, onNext: () -> Unit, onHome: () -> Unit) {
    BackHandler { onHome() }
    val run = game.lastCompleted.value

    Box(
        Modifier
            .fillMaxSize()
            .background(TesseraColors.SplashBg)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("SOLVED", style = TesseraType.display.copy(color = TesseraColors.Paper))
            if (run != null) {
                Text(run.puzzle.name.uppercase(), style = TesseraType.heading.copy(color = TesseraColors.Sky))
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Stat("TIME", fmt(run.elapsedMillis))
                    Stat("MOVES", run.moves.toString())
                    Stat("GRID", "${run.difficulty.gridSize}×${run.difficulty.gridSize}")
                }
            }
            Spacer(Modifier.height(8.dp))
            BlueprintButton("Next puzzle", onNext, Modifier.fillMaxWidth())
            BlueprintButton("Home", onHome, Modifier.fillMaxWidth(), filled = false, foreground = TesseraColors.Paper)
        }
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = TesseraType.label.copy(color = TesseraColors.Faint))
        Text(value, style = TesseraType.heading.copy(color = TesseraColors.Paper))
    }
}
