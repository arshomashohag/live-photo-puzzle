package com.tessera.puzzle.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tessera.puzzle.domain.model.feedback.CompleteSound
import com.tessera.puzzle.domain.model.feedback.MoveSound
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
            .verticalScroll(rememberScrollState())
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

        Column(
            Modifier
                .fillMaxWidth()
                .then(if (settings.soundEnabled) Modifier else Modifier.alpha(0.4f)),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SoundPicker(
                label = "Move",
                current = settings.moveSound.label,
                enabled = settings.soundEnabled,
                options = MoveSound.entries,
                selected = settings.moveSound,
                optionLabel = { it.label },
                optionDescription = { it.description },
                onSelect = { vm.setMoveSound(it) },
            )
            SoundPicker(
                label = "Completion",
                current = settings.completeSound.label,
                enabled = settings.soundEnabled,
                options = CompleteSound.entries,
                selected = settings.completeSound,
                optionLabel = { it.label },
                optionDescription = { it.description },
                onSelect = { vm.setCompleteSound(it) },
            )
        }

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

/**
 * A collapsible sound picker. Collapsed, the header row shows the current
 * selection; expanded, the whole option list is shown at once (no inner
 * scroll — the drawer scrolls as a whole). Tapping an option selects it, and
 * [onSelect] also previews the sound. Disabled when the Sound toggle is off.
 */
@Composable
private fun <T> SoundPicker(
    label: String,
    current: String,
    enabled: Boolean,
    options: List<T>,
    selected: T,
    optionLabel: (T) -> String,
    optionDescription: (T) -> String,
    onSelect: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(TesseraColors.Surface)
            .semantics { contentDescription = "$label sound: $current" },
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable(enabled = enabled) { expanded = !expanded }
                .heightIn(min = 48.dp)
                .padding(horizontal = 13.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = TesseraType.cardTitle, modifier = Modifier.weight(1f))
            Text(current, style = TesseraType.label.copy(color = TesseraColors.Primary))
            Icon(
                imageVector = Icons.Outlined.KeyboardArrowDown,
                contentDescription = null,
                tint = TesseraColors.Faint,
                modifier = Modifier
                    .padding(start = 6.dp)
                    .size(18.dp)
                    .rotate(if (expanded) 180f else 0f),
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                options.forEach { option ->
                    SoundOptionRow(
                        name = optionLabel(option),
                        description = optionDescription(option),
                        selected = option == selected,
                        onClick = { onSelect(option) },
                    )
                }
            }
        }
    }
}

/** One selectable sound option: preview button, name + descriptor, check. */
@Composable
private fun SoundOptionRow(
    name: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (selected) TesseraColors.Primary else TesseraColors.Surface
    val fg = if (selected) TesseraColors.OnPrimary else TesseraColors.Ink
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(bg)
            .clickable { onClick() }
            .heightIn(min = 48.dp)
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .semantics { contentDescription = "$name sound${if (selected) ", selected" else ""}" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(
                    if (selected) TesseraColors.OnPrimary.copy(alpha = 0.22f)
                    else TesseraColors.SurfaceAlt,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.PlayArrow,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(17.dp),
            )
        }
        Column(Modifier.weight(1f)) {
            Text(name, style = TesseraType.cardTitle.copy(color = fg))
            Text(
                description,
                style = TesseraType.label.copy(
                    color = if (selected) fg.copy(alpha = 0.85f) else TesseraColors.Faint,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (selected) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
