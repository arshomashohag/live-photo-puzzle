package com.tessera.puzzle.ui.screens.create

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tessera.puzzle.domain.model.PuzzleNameInput
import com.tessera.puzzle.ui.screens.BackBar
import com.tessera.puzzle.ui.theme.PillButton
import com.tessera.puzzle.ui.theme.TesseraColors
import com.tessera.puzzle.ui.theme.TesseraType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Name-your-puzzle step: shown after Review, before Pick Size. The captured
 * photo is previewed with the typed name overlaid single-line (ellipsis-
 * trimmed, never wrapped). The field caps at [PuzzleNameInput.MAX_LEN]; a blank
 * name falls back to the auto-name downstream (see [confirmName] callers).
 */
@Composable
fun NameScreen(
    source: Uri,
    onConfirm: (String) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var preview by remember(source) { mutableStateOf<ImageBitmap?>(null) }
    var name by remember(source) { mutableStateOf("") }

    LaunchedEffect(source) {
        preview = withContext(Dispatchers.IO) {
            runCatching {
                val opts = BitmapFactory.Options().apply { inSampleSize = 2 }
                context.contentResolver.openInputStream(source)?.use {
                    BitmapFactory.decodeStream(it, null, opts)?.asImageBitmap()
                }
            }.getOrNull()
        }
    }

    // Inset for system bars only (not the IME) at the screen level. The input
    // area below handles the keyboard itself via imePadding(), so the preview
    // stays put and only the field rides up to sit on top of the keyboard.
    Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars)) {
        BackBar("NAME YOUR PUZZLE", onBack)
        Box(Modifier.fillMaxSize()) {
            // Preview: fixed at the top, full-width square. It never moves; the
            // input area floats over it when the keyboard is up.
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .align(Alignment.TopCenter)
                    .background(TesseraColors.Sky),
            ) {
                preview?.let {
                    Image(
                        bitmap = it,
                        contentDescription = "Captured photo preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                // The name as it will appear on the puzzle: single line, trimmed
                // with an ellipsis to the preview width — never wraps.
                if (name.isNotBlank()) {
                    Box(
                        Modifier.fillMaxWidth().align(Alignment.BottomStart)
                            .background(NameScrim)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Text(
                            name,
                            style = TesseraType.cardTitle.copy(color = TesseraColors.OnPrimary),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            // Input area: pinned to the bottom, lifted above the keyboard by
            // imePadding() so it overlays the preview and stays fully visible.
            Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .imePadding()
                    .background(TesseraColors.Haze)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { input ->
                        // Enforce the cap as the user types; strip line breaks so
                        // the name can never wrap. Trimming for storage happens on
                        // confirm.
                        name = input.replace("\n", "").take(PuzzleNameInput.MAX_LEN)
                    },
                    singleLine = true,
                    label = { Text("Puzzle name") },
                    placeholder = { Text("e.g. Beach Day") },
                    supportingText = {
                        Text(
                            "${name.length}/${PuzzleNameInput.MAX_LEN} · leave blank to auto-name",
                            style = TesseraType.label.copy(color = TesseraColors.Muted),
                        )
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TesseraColors.Primary,
                        unfocusedBorderColor = TesseraColors.Hairline,
                        focusedLabelColor = TesseraColors.Primary,
                        cursorColor = TesseraColors.Primary,
                        focusedTextColor = TesseraColors.Ink,
                        unfocusedTextColor = TesseraColors.Ink,
                        focusedContainerColor = TesseraColors.Paper,
                        unfocusedContainerColor = TesseraColors.Paper,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                PillButton("Continue", { onConfirm(name) }, Modifier.fillMaxWidth())
            }
        }
    }
}

/** Bottom gradient-strength scrim behind the overlaid name for legibility. */
private val NameScrim = Color(0x99000000)
