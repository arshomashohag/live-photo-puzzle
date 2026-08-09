package com.tessera.puzzle.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun RegistrationFrame(
    modifier: Modifier = Modifier,
    cornerColor: Color = TesseraColors.Steel,
    borderColor: Color = TesseraColors.Hairline,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, borderColor, RectangleShape),
        )
        content()
        CornerMark(cornerColor, Alignment.TopStart)
        CornerMark(cornerColor, Alignment.TopEnd)
        CornerMark(cornerColor, Alignment.BottomStart)
        CornerMark(cornerColor, Alignment.BottomEnd)
    }
}

@Composable
private fun BoxScope.CornerMark(color: Color, alignment: Alignment) {
    Text(
        text = "+",
        style = TesseraType.mono.copy(color = color),
        modifier = Modifier.align(alignment),
    )
}

@Composable
fun BlueprintButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = true,
    foreground: Color = TesseraColors.Ink,
) {
    val bg = if (filled) TesseraColors.Steel else Color.Transparent
    val fg = if (filled) TesseraColors.Paper else foreground
    Row(
        modifier = modifier
            .background(bg)
            .then(if (filled) Modifier else Modifier.border(1.dp, TesseraColors.Hairline))
            .clickableNoRipple(onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text.uppercase(),
            style = TesseraType.heading.copy(color = fg),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun DifficultyMeter(level: Int, modifier: Modifier = Modifier) {
    Row(modifier) {
        repeat(3) { i ->
            Box(
                Modifier
                    .padding(end = 3.dp)
                    .width(22.dp)
                    .height(4.dp)
                    .background(if (i < level) TesseraColors.Steel else TesseraColors.Hairline),
            )
        }
    }
}

@Composable
fun GridPreview(gridSize: Int, modifier: Modifier = Modifier) {
    Box(
        modifier
            .background(TesseraColors.Sky)
            .drawBehind {
                val step = size.width / gridSize
                val line = 1.dp.toPx()
                val c = Color(0xB3F2F2F3)
                for (i in 1 until gridSize) {
                    drawRect(c, topLeft = Offset(i * step, 0f), size = Size(line, size.height))
                    drawRect(c, topLeft = Offset(0f, i * step), size = Size(size.width, line))
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
