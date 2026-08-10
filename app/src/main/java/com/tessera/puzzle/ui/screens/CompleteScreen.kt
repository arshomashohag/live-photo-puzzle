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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tessera.puzzle.domain.model.Difficulty
import com.tessera.puzzle.game.GameViewModel
import com.tessera.puzzle.ui.theme.PillButton
import com.tessera.puzzle.ui.theme.TesseraColors
import com.tessera.puzzle.ui.theme.TesseraType

private fun fmt(ms: Long): String {
    val s = ms / 1000
    return "%02d:%02d".format(s / 60, s % 60)
}

@Composable
fun CompleteScreen(
    game: GameViewModel,
    onNext: (Difficulty) -> Unit,
    onHome: () -> Unit,
) {
    BackHandler { onHome() }
    val run by game.completeUiState.collectAsStateWithLifecycle()

    Box(
        Modifier
            .fillMaxSize()
            .background(TesseraColors.heroGradient)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("SOLVED", style = TesseraType.display.copy(color = TesseraColors.OnHero))
            run?.let { r ->
                Text(
                    r.puzzleName.uppercase(),
                    style = TesseraType.heading.copy(color = TesseraColors.OnHero.copy(alpha = 0.92f)),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Stat("TIME", fmt(r.elapsedMillis))
                    Stat("MOVES", r.moves.toString())
                    Stat("GRID", "${r.difficulty.gridSize}×${r.difficulty.gridSize}")
                }
                r.best?.let { b ->
                    Text(
                        "BEST ${fmt(b.bestTimeMillis)} · ${b.bestMoves} MOVES · SOLVED ${b.solvedCount}×",
                        style = TesseraType.label.copy(color = TesseraColors.OnHero.copy(alpha = 0.85f)),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            PillButton(
                "Next puzzle",
                { run?.let { onNext(it.difficulty) } ?: onHome() },
                Modifier.fillMaxWidth(),
            )
            PillButton("Home", onHome, Modifier.fillMaxWidth(), filled = false, foreground = TesseraColors.Ink)
        }
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = TesseraType.label.copy(color = TesseraColors.OnHero.copy(alpha = 0.75f)))
        Text(value, style = TesseraType.heading.copy(color = TesseraColors.OnHero))
    }
}
