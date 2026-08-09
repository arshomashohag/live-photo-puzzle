package com.tessera.puzzle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tessera.puzzle.domain.model.persistence.ThemeMode
import com.tessera.puzzle.presentation.SettingsViewModel
import com.tessera.puzzle.ui.theme.TesseraColors
import com.tessera.puzzle.ui.theme.TesseraType

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val vm: SettingsViewModel = hiltViewModel()
    val settings by vm.settings.collectAsStateWithLifecycle()
    var confirmReset by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
        BackBar("SETTINGS", onBack)
        Column(
            Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            SectionLabel("THEME")
            ThemeMode.entries.forEach { mode ->
                ThemeOption(
                    label = mode.name.lowercase().replaceFirstChar { it.uppercase() },
                    selected = settings.theme == mode,
                    onClick = { vm.setTheme(mode) },
                )
            }

            SectionLabel("FEEDBACK")
            ToggleRow(
                "Sound", settings.soundEnabled, "Coming soon",
                onToggle = { vm.setSound(it) },
            )
            ToggleRow(
                "Haptics", settings.hapticsEnabled, "Coming soon",
                onToggle = { vm.setHaptics(it) },
            )

            SectionLabel("DATA")
            Box(
                Modifier.fillMaxWidth().heightIn(min = 56.dp)
                    .clickable { confirmReset = true }
                    .semantics { contentDescription = "Reset statistics" }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text("RESET STATISTICS", style = TesseraType.cardTitle.copy(color = TesseraColors.Steel))
            }
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Reset statistics?") },
            text = { Text("Best times and solved counts will be cleared. Your saved puzzles are kept.") },
            confirmButton = {
                TextButton(onClick = { vm.resetStats(); confirmReset = false }) { Text("RESET") }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) { Text("CANCEL") }
            },
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = TesseraType.label.copy(color = TesseraColors.Faint))
}

@Composable
private fun ThemeOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 48.dp)
            .clickable { onClick() }
            .semantics { contentDescription = "$label theme" },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.padding(end = 12.dp).background(
                if (selected) TesseraColors.Steel else TesseraColors.Hairline,
            ).heightIn(min = 16.dp).padding(8.dp),
        )
        Text(label, style = TesseraType.cardTitle)
    }
}

@Composable
private fun ToggleRow(title: String, checked: Boolean, note: String, onToggle: (Boolean) -> Unit) {
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
