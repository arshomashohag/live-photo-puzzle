package com.tessera.puzzle.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Role-based color scheme resolved per theme. Screens read [TesseraColors]
 * (unchanged names) which now flips light/dark via [LocalTesseraColors].
 */
data class TesseraColorScheme(
    val ink: Color,        // primary text / on-surface
    val paper: Color,      // surface
    val haze: Color,       // canvas
    val steel: Color,      // primary accent
    val steelDeep: Color,
    val sky: Color,
    val mist: Color,
    val muted: Color,
    val faint: Color,
    val hairline: Color,
    val splashBg: Color,   // always-dark splash/overlay canvas
)

val lightScheme = TesseraColorScheme(
    ink = Color(0xFF1D1F20),
    paper = Color(0xFFF2F2F3),
    haze = Color(0xFFE7E7EA),
    steel = Color(0xFF5980A6),
    steelDeep = Color(0xFF2C455D),
    sky = Color(0xFF94BCE3),
    mist = Color(0xFFD6EBFF),
    muted = Color(0xFF5D5D60),
    faint = Color(0xFF7A7A7D),
    hairline = Color(0x471D1F20), // ink @ 28%
    splashBg = Color(0xFF1D2D3D),
)

val darkScheme = TesseraColorScheme(
    ink = Color(0xFFE7ECF1),      // on-dark text
    paper = Color(0xFF1D2D3D),    // dark surface
    haze = Color(0xFF14202B),     // dark canvas
    steel = Color(0xFF6E97BE),    // slightly lifted accent for dark contrast
    steelDeep = Color(0xFF2C455D),
    sky = Color(0xFF94BCE3),
    mist = Color(0xFF3A5570),
    muted = Color(0xFF9DA9B4),
    faint = Color(0xFF7E8A95),
    hairline = Color(0x47E7ECF1), // paper @ 28%
    splashBg = Color(0xFF14202B),
)

val LocalTesseraColors = staticCompositionLocalOf { lightScheme }

/**
 * Backwards-compatible accessor: existing `TesseraColors.Steel` etc. now resolve
 * from the current theme. All reads happen inside composables.
 */
object TesseraColors {
    val Ink: Color @Composable @ReadOnlyComposable get() = LocalTesseraColors.current.ink
    val Paper: Color @Composable @ReadOnlyComposable get() = LocalTesseraColors.current.paper
    val Haze: Color @Composable @ReadOnlyComposable get() = LocalTesseraColors.current.haze
    val Steel: Color @Composable @ReadOnlyComposable get() = LocalTesseraColors.current.steel
    val SteelDeep: Color @Composable @ReadOnlyComposable get() = LocalTesseraColors.current.steelDeep
    val Sky: Color @Composable @ReadOnlyComposable get() = LocalTesseraColors.current.sky
    val Mist: Color @Composable @ReadOnlyComposable get() = LocalTesseraColors.current.mist
    val Muted: Color @Composable @ReadOnlyComposable get() = LocalTesseraColors.current.muted
    val Faint: Color @Composable @ReadOnlyComposable get() = LocalTesseraColors.current.faint
    val Hairline: Color @Composable @ReadOnlyComposable get() = LocalTesseraColors.current.hairline
    val SplashBg: Color @Composable @ReadOnlyComposable get() = LocalTesseraColors.current.splashBg
}
