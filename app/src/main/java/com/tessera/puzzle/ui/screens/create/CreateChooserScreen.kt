package com.tessera.puzzle.ui.screens.create

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.tessera.puzzle.ui.screens.BackBar
import com.tessera.puzzle.ui.theme.TesseraColors
import com.tessera.puzzle.ui.theme.TesseraType

@Composable
fun CreateChooserScreen(
    onTakePhoto: () -> Unit,
    onChoosePhoto: () -> Unit,
    onBack: () -> Unit,
) {
    Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
        BackBar("CREATE A PUZZLE", onBack)
        Column(
            Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ChooserButton("TAKE PHOTO", onTakePhoto, filled = true)
            ChooserButton("CHOOSE PHOTO", onChoosePhoto, filled = false)
            Text(
                "Your photo stays on this device.",
                style = TesseraType.body.copy(color = TesseraColors.Muted),
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun ChooserButton(text: String, onClick: () -> Unit, filled: Boolean) {
    val bg = if (filled) TesseraColors.Steel else TesseraColors.Paper
    val fg = if (filled) TesseraColors.Paper else TesseraColors.Ink
    Box(
        Modifier
            .fillMaxWidth().height(64.dp).background(bg)
            .clickable { onClick() }
            .semantics { contentDescription = text },
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = TesseraType.heading.copy(color = fg))
    }
}
