package com.tessera.puzzle.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.tessera.puzzle.domain.model.LevelAccentKey

private fun materialSchemeFrom(s: TesseraColorScheme, dark: Boolean) =
    if (dark) {
        darkColorScheme(
            primary = s.primary,
            onPrimary = s.onPrimary,
            background = s.canvas,
            onBackground = s.ink,
            surface = s.surface,
            onSurface = s.ink,
        )
    } else {
        lightColorScheme(
            primary = s.primary,
            onPrimary = s.onPrimary,
            background = s.canvas,
            onBackground = s.ink,
            surface = s.surface,
            onSurface = s.ink,
        )
    }

@Composable
fun TesseraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val scheme = if (darkTheme) darkScheme else lightScheme
    CompositionLocalProvider(LocalTesseraColors provides scheme) {
        MaterialTheme(
            colorScheme = materialSchemeFrom(scheme, darkTheme),
            content = content,
        )
    }
}

/**
 * Resolves a per-level accent key to the current theme's color.
 */
@Composable
@ReadOnlyComposable
fun accentColor(key: LevelAccentKey): Color = when (key) {
    LevelAccentKey.TEAL -> LocalTesseraColors.current.teal
    LevelAccentKey.CORAL -> LocalTesseraColors.current.primary
    LevelAccentKey.PURPLE -> LocalTesseraColors.current.purple
}
