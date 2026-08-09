package com.tessera.puzzle.ui.screens.create

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.tessera.puzzle.domain.model.Difficulty
import com.tessera.puzzle.ui.screens.BackBar
import com.tessera.puzzle.ui.theme.DifficultyMeter
import com.tessera.puzzle.ui.theme.GridPreview
import com.tessera.puzzle.ui.theme.RegistrationFrame
import com.tessera.puzzle.ui.theme.TesseraColors
import com.tessera.puzzle.ui.theme.TesseraType

@Composable
fun PickSizeScreen(onPick: (Difficulty) -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
        BackBar("PICK A SIZE", onBack)
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Difficulty.entries.forEach { d ->
                RegistrationFrame(
                    Modifier.fillMaxWidth().height(120.dp)
                        .clickable { onPick(d) }
                        .semantics { contentDescription = "${d.label}, ${d.gridSize} by ${d.gridSize}" },
                ) {
                    androidx.compose.foundation.layout.Row(
                        Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        GridPreview(d.gridSize, Modifier.width(96.dp).height(96.dp))
                        androidx.compose.foundation.layout.Spacer(Modifier.width(14.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(d.label.uppercase(), style = TesseraType.heading)
                            Text("${d.gridSize} × ${d.gridSize} · ${d.tileCount} tiles",
                                style = TesseraType.body.copy(color = TesseraColors.Muted))
                            DifficultyMeter(d.level)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ImportGeneratingScreen() {
    val transition = rememberInfiniteTransition(label = "gen")
    val pulse by transition.animateFloat(
        initialValue = 0.35f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "pulse",
    )
    Box(
        Modifier.fillMaxSize().background(TesseraColors.SplashBg)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Box(Modifier.width(96.dp).height(96.dp).alpha(pulse).background(TesseraColors.Paper))
            Text("GENERATING", style = TesseraType.display.copy(color = TesseraColors.Paper))
            Text("Slicing your photo into tiles", style = TesseraType.label.copy(color = TesseraColors.Sky))
        }
    }
}

@Composable
fun CreateErrorScreen(message: String, onDismiss: () -> Unit) {
    Box(
        Modifier.fillMaxSize().background(TesseraColors.Haze)
            .windowInsetsPadding(WindowInsets.safeDrawing).padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(message, style = TesseraType.body)
            Box(
                Modifier.fillMaxWidth().height(56.dp).background(TesseraColors.Steel)
                    .clickable { onDismiss() }
                    .semantics { contentDescription = "OK" },
                contentAlignment = Alignment.Center,
            ) { Text("OK", style = TesseraType.heading.copy(color = TesseraColors.Paper)) }
        }
    }
}

@Composable
fun PermissionNeededScreen(
    onOpenSettings: () -> Unit,
    onChoosePhoto: () -> Unit,
    onBack: () -> Unit,
) {
    Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
        BackBar("CAMERA ACCESS NEEDED", onBack)
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                "Tessera needs camera access to take a photo for your puzzle. " +
                    "You can enable it in Settings, or choose an existing photo instead.",
                style = TesseraType.body,
            )
            Box(
                Modifier.fillMaxWidth().height(56.dp).background(TesseraColors.Steel)
                    .clickable { onOpenSettings() }
                    .semantics { contentDescription = "Open settings" },
                contentAlignment = Alignment.Center,
            ) { Text("OPEN SETTINGS", style = TesseraType.heading.copy(color = TesseraColors.Paper)) }
            Box(
                Modifier.fillMaxWidth().height(56.dp).background(TesseraColors.Paper)
                    .clickable { onChoosePhoto() }
                    .semantics { contentDescription = "Choose from photos" },
                contentAlignment = Alignment.Center,
            ) { Text("CHOOSE FROM PHOTOS", style = TesseraType.heading.copy(color = TesseraColors.Ink)) }
        }
    }
}
