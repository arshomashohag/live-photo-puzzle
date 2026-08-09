package com.tessera.puzzle.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val TesseraColorScheme = lightColorScheme(
    primary = TesseraColors.Steel,
    onPrimary = TesseraColors.Paper,
    background = TesseraColors.Paper,
    onBackground = TesseraColors.Ink,
    surface = TesseraColors.Paper,
    onSurface = TesseraColors.Ink,
)

@Composable
fun TesseraTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = TesseraColorScheme, content = content)
}
