package com.tessera.puzzle.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.tessera.puzzle.domain.model.Difficulty
import com.tessera.puzzle.domain.model.layoutSpec
import com.tessera.puzzle.domain.model.persistence.ImageRef
import com.tessera.puzzle.game.GameViewModel
import com.tessera.puzzle.ui.theme.rememberWindowSize
import com.tessera.puzzle.presentation.PuzzleListItem
import com.tessera.puzzle.ui.theme.RegistrationFrame
import com.tessera.puzzle.ui.theme.TesseraColors
import com.tessera.puzzle.ui.theme.TesseraType

@Composable
fun PuzzleSelectScreen(
    game: GameViewModel,
    difficulty: Difficulty,
    onBack: () -> Unit,
    onPick: (String) -> Unit,
) {
    val puzzles by game.puzzles.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
        BackBar(
            "${difficulty.label.uppercase()} · ${difficulty.gridSize} × ${difficulty.gridSize}",
            onBack,
        )
        val columns = layoutSpec(rememberWindowSize()).gridColumns
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            contentPadding = PaddingValues(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(puzzles, key = { it.puzzle.id }) { item ->
                PuzzleCard(item, difficulty, onPick)
            }
        }
    }
}

@Composable
private fun PuzzleCard(
    item: PuzzleListItem,
    difficulty: Difficulty,
    onPick: (String) -> Unit,
) {
    val context = LocalContext.current
    val p = item.puzzle
    val resumable = difficulty in item.resumableDifficulties
    RegistrationFrame(Modifier.fillMaxWidth().clickable { onPick(p.id) }) {
        Column {
            Box {
                when (val ref = p.imageRef) {
                    is ImageRef.DrawableRef -> {
                        val resId = context.resources.getIdentifier(
                            ref.resName, "drawable", context.packageName,
                        )
                        if (resId != 0) {
                            Image(
                                painter = painterResource(resId),
                                contentDescription = p.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                            )
                        } else {
                            Box(Modifier.fillMaxWidth().aspectRatio(1f).background(TesseraColors.Sky))
                        }
                    }
                    is ImageRef.FileRef -> {
                        // Load the custom puzzle's thumbnail from app-internal
                        // storage (same pattern as MyPuzzlesScreen). The Sky box
                        // shows only while decoding / if the file is missing.
                        val thumbPath = ref.thumbPath
                        var thumb by remember(thumbPath) { mutableStateOf<ImageBitmap?>(null) }
                        LaunchedEffect(thumbPath) {
                            thumb = withContext(Dispatchers.IO) {
                                runCatching {
                                    BitmapFactory.decodeFile(thumbPath)?.asImageBitmap()
                                }.getOrNull()
                            }
                        }
                        Box(Modifier.fillMaxWidth().aspectRatio(1f).background(TesseraColors.Sky)) {
                            thumb?.let {
                                Image(
                                    bitmap = it,
                                    contentDescription = p.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                    }
                }
                if (resumable) {
                    Box(
                        Modifier.padding(6.dp).background(TesseraColors.SteelDeep).padding(horizontal = 5.dp, vertical = 3.dp),
                    ) {
                        Text("RESUME", style = TesseraType.label.copy(color = TesseraColors.OnPrimary))
                    }
                }
            }
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
