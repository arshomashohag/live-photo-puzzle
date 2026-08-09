package com.tessera.puzzle.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tessera.puzzle.data.PuzzleCatalog
import com.tessera.puzzle.model.Difficulty
import com.tessera.puzzle.model.Puzzle
import com.tessera.puzzle.ui.theme.RegistrationFrame
import com.tessera.puzzle.ui.theme.TesseraColors
import com.tessera.puzzle.ui.theme.TesseraType

@Composable
fun PuzzleSelectScreen(
    difficulty: Difficulty,
    onBack: () -> Unit,
    onPick: (Puzzle) -> Unit,
) {
    val puzzles = PuzzleCatalog.forDifficulty(difficulty)
    Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
        BackBar(
            "${difficulty.label.uppercase()} · ${difficulty.gridSize} × ${difficulty.gridSize}",
            onBack,
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(puzzles, key = { it.id }) { p ->
                RegistrationFrame(Modifier.fillMaxWidth().clickable { onPick(p) }) {
                    Column {
                        Image(
                            painter = painterResource(p.imageRes),
                            contentDescription = p.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                        )
                        Column(Modifier.padding(10.dp)) {
                            Text(p.name.uppercase(), style = TesseraType.cardTitle)
                            Text(
                                "${difficulty.gridSize} × ${difficulty.gridSize}",
                                style = TesseraType.body.copy(color = TesseraColors.Muted),
                            )
                        }
                    }
                }
            }
        }
    }
}
