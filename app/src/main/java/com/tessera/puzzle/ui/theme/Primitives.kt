package com.tessera.puzzle.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * v2 rounded, shadowed card. (Keeps the old name `RegistrationFrame` so existing
 * screens compile; the blueprint corner-marks are gone.)
 */
@Composable
fun RegistrationFrame(
    modifier: Modifier = Modifier,
    cornerColor: Color = TesseraColors.Primary,
    borderColor: Color = TesseraColors.Hairline,
    content: @Composable () -> Unit,
) {
    RoundedCard(modifier = modifier, content = content)
}

@Composable
fun RoundedCard(
    modifier: Modifier = Modifier,
    color: Color = TesseraColors.Surface,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .cardShadow(TesseraShapes.card)
            .clip(TesseraShapes.card)
            .background(color),
    ) {
        content()
    }
}

/**
 * v2 pill button. Kept as `BlueprintButton` name for call-site stability; also
 * exposed as `PillButton`.
 */
@Composable
fun BlueprintButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = true,
    foreground: Color = TesseraColors.Ink,
) = PillButton(text, onClick, modifier, filled, foreground)

@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = true,
    foreground: Color = TesseraColors.Ink,
) {
    val bg = if (filled) TesseraColors.Primary else TesseraColors.SurfaceAlt
    val fg = if (filled) TesseraColors.OnPrimary else foreground
    Row(
        modifier = modifier
            .then(if (filled) Modifier.primaryGlow(TesseraShapes.pill) else Modifier)
            .clip(TesseraShapes.pill)
            .background(bg)
            .clickableNoRipple(onClick)
            .heightIn(min = 48.dp)
            .padding(horizontal = 22.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, style = TesseraType.heading.copy(color = fg), textAlign = TextAlign.Center)
    }
}

@Composable
fun Chip(text: String, accent: Color = TesseraColors.Primary, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(TesseraShapes.chip)
            .background(accent.copy(alpha = 0.16f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(text, style = TesseraType.label.copy(color = accent))
    }
}

@Composable
fun DifficultyMeter(level: Int, modifier: Modifier = Modifier, accent: Color = TesseraColors.Primary) {
    Row(modifier) {
        repeat(3) { i ->
            Box(
                Modifier
                    .padding(end = 4.dp)
                    .width(22.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (i < level) accent else TesseraColors.Hairline),
            )
        }
    }
}

@Composable
fun GridPreview(gridSize: Int, modifier: Modifier = Modifier, accent: Color = TesseraColors.Primary) {
    val lineColor = TesseraColors.OnPrimary.copy(alpha = 0.55f)
    Box(
        modifier
            .clip(TesseraShapes.tile)
            .background(accent)
            .drawBehind {
                val step = size.width / gridSize
                val line = 2.dp.toPx()
                for (i in 1 until gridSize) {
                    drawRect(lineColor, topLeft = Offset(i * step, 0f), size = Size(line, size.height))
                    drawRect(lineColor, topLeft = Offset(0f, i * step), size = Size(size.width, line))
                }
            },
    )
}

fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick,
    )
}
