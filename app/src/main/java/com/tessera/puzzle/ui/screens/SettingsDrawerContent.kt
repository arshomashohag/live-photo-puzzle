package com.tessera.puzzle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tessera.puzzle.domain.model.persistence.ThemeMode
import com.tessera.puzzle.presentation.SettingsViewModel
import com.tessera.puzzle.ui.theme.TesseraColors
import com.tessera.puzzle.ui.theme.TesseraDialog
import com.tessera.puzzle.ui.theme.TesseraShapes
import com.tessera.puzzle.ui.theme.TesseraType

@Composable
fun SettingsDrawerContent() {
    val vm: SettingsViewModel = hiltViewModel()
    val settings by vm.settings.collectAsStateWithLifecycle()
    var confirmReset by remember { mutableStateOf(false) }

    Column(
        Modifier
            .width(300.dp)
            .fillMaxHeight()
            .background(TesseraColors.Surface)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text("Settings", style = TesseraType.heading.copy(color = TesseraColors.Ink))

        Text("THEME", style = TesseraType.label.copy(color = TesseraColors.Faint))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeMode.entries.forEach { mode ->
                val selected = settings.theme == mode
                Box(
                    Modifier
                        .clip(TesseraShapes.pill)
                        .background(if (selected) TesseraColors.Primary else TesseraColors.SurfaceAlt)
                        .clickable { vm.setTheme(mode) }
                        .heightIn(min = 40.dp)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .semantics { contentDescription = "${mode.name.lowercase()} theme" },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        mode.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = TesseraType.label.copy(
                            color = if (selected) TesseraColors.OnPrimary else TesseraColors.Ink,
                        ),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        Text("FEEDBACK", style = TesseraType.label.copy(color = TesseraColors.Faint))
        DrawerToggle("Sound", settings.soundEnabled, "Tile & completion sounds") { vm.setSound(it) }
        DrawerToggle("Haptics", settings.hapticsEnabled, "Vibrate on moves") { vm.setHaptics(it) }

        Text("DATA", style = TesseraType.label.copy(color = TesseraColors.Faint))
        Box(
            Modifier
                .fillMaxWidth().clip(RoundedCornerShape(14.dp))
                .background(TesseraColors.Pink.copy(alpha = 0.12f))
                .clickable { confirmReset = true }
                .heightIn(min = 48.dp)
                .padding(14.dp)
                .semantics { contentDescription = "Reset statistics" },
            contentAlignment = Alignment.CenterStart,
        ) {
            Text("Reset statistics", style = TesseraType.cardTitle.copy(color = TesseraColors.Pink))
        }
    }

    if (confirmReset) {
        TesseraDialog(
            title = "Reset statistics?",
            message = "Best times and solved counts will be cleared. Your saved puzzles are kept.",
            confirmLabel = "RESET",
            destructive = true,
            onConfirm = { vm.resetStats(); confirmReset = false },
            onDismiss = { confirmReset = false },
        )
    }
}

@Composable
private fun DrawerToggle(title: String, checked: Boolean, note: String, onToggle: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = TesseraType.cardTitle)
            Text(note, style = TesseraType.label.copy(color = TesseraColors.Faint))
        }
        Switch(checked = checked, onCheckedChange = onToggle)
    }
}
