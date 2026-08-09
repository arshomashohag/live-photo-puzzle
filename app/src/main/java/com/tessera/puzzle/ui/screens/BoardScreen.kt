package com.tessera.puzzle.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tessera.puzzle.domain.model.Difficulty
import com.tessera.puzzle.game.GameViewModel
import com.tessera.puzzle.ui.theme.BlueprintButton
import com.tessera.puzzle.ui.theme.TesseraColors
import com.tessera.puzzle.ui.theme.TesseraType

private fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    return "%02d:%02d".format(totalSec / 60, totalSec % 60)
}

@Composable
fun BoardScreen(
    game: GameViewModel,
    puzzleId: String,
    difficulty: Difficulty,
    onSolved: () -> Unit,
    onExit: () -> Unit,
) {
    val state by game.boardUiState.collectAsStateWithLifecycle()
    val board = state.board
    val tiles = state.tiles
    var paused by remember { mutableStateOf(false) }

    LaunchedEffect(puzzleId, difficulty) {
        val b = game.boardUiState.value.board
        if (b == null || b.puzzle.id != puzzleId || b.difficulty != difficulty || b.isSolved) {
            game.startBoard(puzzleId, difficulty)
        }
    }

    LaunchedEffect(board?.isSolved) {
        if (board != null && board.isSolved) onSolved()
    }

    // Forced save on lifecycle stop (BR-2).
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) game.flushSave()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    BackHandler(enabled = !paused) { paused = true }

    if (board == null) {
        Box(Modifier.fillMaxSize().background(TesseraColors.Haze))
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(TesseraColors.Haze)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    "${board.puzzle.name.uppercase()} · ${difficulty.label.uppercase()}",
                    style = TesseraType.cardTitle,
                )
                Text(
                    "${board.placedCount}/${difficulty.tileCount} PLACED",
                    style = TesseraType.label.copy(color = TesseraColors.Faint),
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatTime(board.elapsedMillis), style = TesseraType.mono.copy(color = TesseraColors.Ink))
                Text("${board.moves} MOVES", style = TesseraType.label.copy(color = TesseraColors.Faint))
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(difficulty.gridSize),
            modifier = Modifier.fillMaxWidth().aspectRatio(1f).border(1.dp, TesseraColors.Ink),
            userScrollEnabled = false,
        ) {
            items(count = board.order.size) { position ->
                val sourceIndex = board.order[position]
                val selected = board.selected == position
                val placed = sourceIndex == position
                Box(
                    Modifier
                        .aspectRatio(1f)
                        .clickable { game.tap(position) }
                        .then(
                            if (selected) Modifier.border(3.dp, TesseraColors.Steel)
                            else Modifier.border(0.5.dp, TesseraColors.Hairline),
                        )
                        .semantics {
                            contentDescription = "Tile ${position + 1}" +
                                (if (selected) ", selected" else "") +
                                (if (placed) ", in place" else "")
                        },
                ) {
                    val tile = tiles.getOrNull(sourceIndex)
                    if (tile != null) {
                        Image(
                            bitmap = tile,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        Box(Modifier.fillMaxSize().background(TesseraColors.Sky))
                    }
                }
            }
        }

        BlueprintButton(
            text = "Pause",
            onClick = { paused = true },
            modifier = Modifier.fillMaxWidth(),
            filled = false,
        )
    }

    if (paused) {
        PauseOverlay(
            onResume = { paused = false },
            onRestart = { paused = false; game.restart() },
            onExit = { paused = false; game.exitBoard(); onExit() },
        )
    }
}

@Composable
private fun PauseOverlay(onResume: () -> Unit, onRestart: () -> Unit, onExit: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(TesseraColors.SplashBg.copy(alpha = 0.92f))
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("PAUSED", style = TesseraType.display.copy(color = TesseraColors.Paper))
            BlueprintButton("Resume", onResume, Modifier.fillMaxWidth())
            BlueprintButton("Restart", onRestart, Modifier.fillMaxWidth(), filled = false, foreground = TesseraColors.Paper)
            BlueprintButton("Exit puzzle", onExit, Modifier.fillMaxWidth(), filled = false, foreground = TesseraColors.Paper)
        }
    }
}
