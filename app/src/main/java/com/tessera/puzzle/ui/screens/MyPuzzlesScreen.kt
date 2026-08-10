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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tessera.puzzle.domain.model.layoutSpec
import com.tessera.puzzle.domain.model.persistence.ImageRef
import com.tessera.puzzle.domain.model.persistence.PuzzleRecord
import com.tessera.puzzle.domain.model.persistence.PuzzleSource
import com.tessera.puzzle.game.GameViewModel
import com.tessera.puzzle.ui.theme.rememberWindowSize
import com.tessera.puzzle.ui.theme.RegistrationFrame
import com.tessera.puzzle.ui.theme.TesseraColors
import com.tessera.puzzle.ui.theme.TesseraDialog
import com.tessera.puzzle.ui.theme.TesseraType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun MyPuzzlesScreen(
    game: GameViewModel,
    onBack: () -> Unit,
    onPlay: (String) -> Unit,
) {
    val all by game.puzzles.collectAsStateWithLifecycle()
    val custom = all.map { it.puzzle }
        .filter { it.source == PuzzleSource.CUSTOM }
        .sortedByDescending { it.createdAt }
    var pendingDelete by remember { mutableStateOf<PuzzleRecord?>(null) }

    Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
        BackBar("MY PUZZLES", onBack)
        if (custom.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(
                    "No custom puzzles yet.\nCreate one from a photo.",
                    style = TesseraType.body.copy(color = TesseraColors.Muted),
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(layoutSpec(rememberWindowSize()).gridColumns),
                contentPadding = PaddingValues(18.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(custom, key = { it.id }) { p ->
                    CustomCard(p, onPlay = { onPlay(p.id) }, onDelete = { pendingDelete = p })
                }
            }
        }
    }

    pendingDelete?.let { target ->
        TesseraDialog(
            title = "Delete this puzzle?",
            message = "\"${target.name}\" will be removed. This can't be undone.",
            confirmLabel = "DELETE",
            destructive = true,
            onConfirm = {
                game.deleteCustomPuzzle(target.id)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
private fun CustomCard(p: PuzzleRecord, onPlay: () -> Unit, onDelete: () -> Unit) {
    val thumbPath = (p.imageRef as? ImageRef.FileRef)?.thumbPath
    var thumb by remember(thumbPath) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(thumbPath) {
        thumb = withContext(Dispatchers.IO) {
            thumbPath?.let { path ->
                runCatching { BitmapFactory.decodeFile(path)?.asImageBitmap() }.getOrNull()
            }
        }
    }
    RegistrationFrame(Modifier.fillMaxWidth().clickable { onPlay() }) {
        Column {
            Box(Modifier.fillMaxWidth().aspectRatio(1f).background(TesseraColors.Sky)) {
                thumb?.let {
                    Image(it, contentDescription = p.name, contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize())
                }
            }
            androidx.compose.foundation.layout.Row(
                Modifier.fillMaxWidth().padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(p.name.uppercase(), style = TesseraType.cardTitle)
                Text(
                    "DELETE",
                    style = TesseraType.label.copy(color = TesseraColors.Steel),
                    modifier = Modifier.clickable { onDelete() }
                        .semantics { contentDescription = "Delete ${p.name}" }
                        .padding(4.dp),
                )
            }
        }
    }
}
