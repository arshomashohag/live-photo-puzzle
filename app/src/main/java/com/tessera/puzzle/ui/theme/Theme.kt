package com.tessera.puzzle.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private fun materialSchemeFrom(s: TesseraColorScheme, dark: Boolean) =
    if (dark) {
        darkColorScheme(
            primary = s.steel,
            onPrimary = s.paper,
            background = s.haze,
            onBackground = s.ink,
            surface = s.paper,
            onSurface = s.ink,
        )
    } else {
        lightColorScheme(
            primary = s.steel,
            onPrimary = s.paper,
            background = s.haze,
            onBackground = s.ink,
            surface = s.paper,
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
