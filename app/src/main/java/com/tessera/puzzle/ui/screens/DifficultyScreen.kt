package com.tessera.puzzle.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.heightIn
import com.tessera.puzzle.domain.model.Difficulty
import com.tessera.puzzle.domain.model.LevelPalette
import com.tessera.puzzle.ui.theme.accentColor
import com.tessera.puzzle.ui.theme.DifficultyMeter
import com.tessera.puzzle.ui.theme.GridPreview
import com.tessera.puzzle.ui.theme.RegistrationFrame
import com.tessera.puzzle.ui.theme.TesseraColors
import com.tessera.puzzle.ui.theme.TesseraType

@Composable
fun BackBar(title: String, onBack: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(56.dp).padding(start = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(48.dp).clickable { onBack() },
            contentAlignment = Alignment.Center,
        ) {
            Text("←", style = TesseraType.heading.copy(color = TesseraColors.Ink))
        }
        Spacer(Modifier.width(6.dp))
        Text(title, style = TesseraType.heading)
    }
}

@Composable
fun DifficultyScreen(onBack: () -> Unit, onPick: (Difficulty) -> Unit) {
    Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
        BackBar("CHOOSE DIFFICULTY", onBack)
        Column(
            Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Difficulty.entries.forEach { d ->
                val accent = accentColor(LevelPalette.accentFor(d))
                RegistrationFrame(
                    Modifier.fillMaxWidth().heightIn(min = 140.dp).clickable { onPick(d) },
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        GridPreview(d.gridSize, Modifier.size(112.dp), accent = accent)
                        Spacer(Modifier.width(14.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    d.label.uppercase(),
                                    style = TesseraType.heading.copy(fontSize = 30.sp),
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "LEVEL ${d.level}/3",
                                    style = TesseraType.label.copy(color = TesseraColors.Faint),
                                )
                            }
                            Text("${d.gridSize} × ${d.gridSize} grid · ${d.tileCount} tiles", style = TesseraType.body)
                            DifficultyMeter(d.level, accent = accent)
                        }
                    }
                }
            }
        }
    }
}
