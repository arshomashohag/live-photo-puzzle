package com.tessera.puzzle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tessera.puzzle.domain.model.Difficulty
import com.tessera.puzzle.game.GameViewModel
import com.tessera.puzzle.presentation.ContinueInfo
import com.tessera.puzzle.ui.theme.DifficultyMeter
import com.tessera.puzzle.ui.theme.GridPreview
import com.tessera.puzzle.ui.theme.RegistrationFrame
import com.tessera.puzzle.ui.theme.TesseraColors
import com.tessera.puzzle.ui.theme.TesseraType

private fun fmtTime(ms: Long?): String {
    if (ms == null) return "—"
    val s = ms / 1000
    return "%02d:%02d".format(s / 60, s % 60)
}

@Composable
fun HomeScreen(
    game: GameViewModel,
    onContinue: (ContinueInfo) -> Unit,
    onPickDifficulty: (Difficulty) -> Unit,
    onCreate: () -> Unit,
    onMyPuzzles: () -> Unit,
) {
    val state by game.homeUiState.collectAsStateWithLifecycle()
    val scroll = rememberScrollState()

    Column(
        Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(scroll)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(32.dp).background(TesseraColors.Steel))
            Spacer(Modifier.width(12.dp))
            Text("TESSERA", style = TesseraType.heading.copy(fontSize = 26.sp, color = TesseraColors.Ink))
        }

        if (state.restoreNotice) {
            Box(
                Modifier.fillMaxWidth().background(TesseraColors.Mist).padding(10.dp)
                    .clickable { game.consumeRestoreNotice() },
            ) {
                Text(
                    "Couldn't restore your last puzzle.",
                    style = TesseraType.body.copy(color = TesseraColors.SteelDeep),
                )
            }
        }

        val cont = state.continueInfo
        if (cont != null) {
            Text("CONTINUE", style = TesseraType.label.copy(color = TesseraColors.Faint))
            RegistrationFrame(
                Modifier.fillMaxWidth().height(110.dp).clickable { onContinue(cont) },
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("${cont.puzzleName} · ${cont.difficulty.label}", style = TesseraType.cardTitle)
                    Text(
                        "${cont.difficulty.gridSize}×${cont.difficulty.gridSize} · " +
                            "${cont.placed}/${cont.total} placed",
                        style = TesseraType.body.copy(color = TesseraColors.Muted),
                    )
                }
            }
        }

        Box(
            Modifier
                .fillMaxWidth().height(60.dp).background(TesseraColors.Steel)
                .clickable { onCreate() },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "CREATE FROM CAMERA",
                style = TesseraType.heading.copy(color = TesseraColors.Paper),
            )
        }

        Text("CHOOSE A DIFFICULTY", style = TesseraType.label.copy(color = TesseraColors.Faint))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Difficulty.entries.forEach { d ->
                RegistrationFrame(
                    Modifier.weight(1f).height(150.dp).clickable { onPickDifficulty(d) },
                ) {
                    Column {
                        GridPreview(d.gridSize, Modifier.fillMaxWidth().height(64.dp))
                        Column(
                            Modifier.padding(9.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(d.label.uppercase(), style = TesseraType.cardTitle)
                            Text(
                                "${d.gridSize} × ${d.gridSize} · ${d.tileCount} tiles",
                                style = TesseraType.body.copy(color = TesseraColors.Muted),
                            )
                            DifficultyMeter(d.level)
                        }
                    }
                }
            }
        }

        Box(
            Modifier.fillMaxWidth().height(56.dp)
                .border(1.dp, TesseraColors.Hairline)
                .clickable { onMyPuzzles() }
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                "MY PUZZLES · ${state.stats.createdCount} SAVED",
                style = TesseraType.cardTitle,
            )
        }

        Row(Modifier.fillMaxWidth().height(64.dp).background(TesseraColors.Paper)) {
            StatCell("SOLVED", state.stats.solvedTotal.toString(), Modifier.weight(1f))
            StatCell("BEST 3×3", fmtTime(state.stats.bestEasyTimeMillis), Modifier.weight(1f))
            StatCell("CREATED", state.stats.createdCount.toString(), Modifier.weight(1f))
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.StatCell(
    label: String,
    value: String,
    modifier: Modifier,
) {
    Column(modifier.padding(12.dp)) {
        Text(label, style = TesseraType.label.copy(color = TesseraColors.Faint))
        Spacer(Modifier.height(6.dp))
        Text(value, style = TesseraType.cardTitle)
    }
}
