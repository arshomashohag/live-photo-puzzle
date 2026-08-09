package com.tessera.puzzle.ui.screens.create

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import android.graphics.BitmapFactory
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.tessera.puzzle.ui.theme.TesseraColors
import com.tessera.puzzle.ui.theme.TesseraType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ReviewScreen(
    source: Uri,
    onRetake: () -> Unit,
    onAccept: () -> Unit,
) {
    val context = LocalContext.current
    var preview by remember(source) { mutableStateOf<ImageBitmap?>(null) }
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
    Column(
        Modifier.fillMaxSize().background(TesseraColors.Haze)
            .windowInsetsPadding(WindowInsets.safeDrawing).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("REVIEW", style = TesseraType.heading)
        Box(Modifier.fillMaxWidth().aspectRatio(1f).background(TesseraColors.Sky)) {
            preview?.let {
                Image(
                    bitmap = it,
                    contentDescription = "Captured photo preview",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        Text(
            "The square area becomes your puzzle.",
            style = TesseraType.body.copy(color = TesseraColors.Muted),
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ReviewButton("RETAKE", onRetake, filled = false, Modifier.weight(1f))
            ReviewButton("ACCEPT", onAccept, filled = true, Modifier.weight(1f))
        }
    }
}

@Composable
private fun ReviewButton(text: String, onClick: () -> Unit, filled: Boolean, modifier: Modifier) {
    val bg = if (filled) TesseraColors.Steel else TesseraColors.Paper
    val fg = if (filled) TesseraColors.Paper else TesseraColors.Ink
    Box(
        modifier.height(56.dp).background(bg).clickable { onClick() }
            .semantics { contentDescription = text },
        contentAlignment = Alignment.Center,
    ) { Text(text, style = TesseraType.heading.copy(color = fg)) }
}
