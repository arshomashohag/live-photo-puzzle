package com.tessera.puzzle.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * v2 role-based color scheme (warm/playful). Resolved per theme via
 * [LocalTesseraColors]; screens read [TesseraColors] (names kept stable so the
 * reskin doesn't churn every call site).
 */
data class TesseraColorScheme(
    val canvas: Color,       // app background
    val surface: Color,      // cards
    val surfaceAlt: Color,   // secondary surface / chips bg
    val ink: Color,          // primary text
    val muted: Color,
    val faint: Color,
    val primary: Color,      // coral
    val primaryLight: Color,
    val primaryDeep: Color,
    val teal: Color,
    val pink: Color,
    val purple: Color,
    val gold: Color,
    val hairline: Color,
    val onPrimary: Color,    // text on primary/gradients
)

val lightScheme = TesseraColorScheme(
    canvas = Color(0xFFFFE9DA),
    surface = Color(0xFFFFF6EF),
    surfaceAlt = Color(0xFFF6EAE2),
    ink = Color(0xFF2E1F1A),
    muted = Color(0xFF7A5C50),
    faint = Color(0xFFA08076),
    primary = Color(0xFFF2603C),
    primaryLight = Color(0xFFFF9E5E),
    primaryDeep = Color(0xFFD64A28),
    teal = Color(0xFF17B892),
    pink = Color(0xFFE0447E),
    purple = Color(0xFF7C5CFF),
    gold = Color(0xFFFF8A2B),
    hairline = Color(0x1A2E1F1A),
    onPrimary = Color(0xFFFFFFFF),
)

val darkScheme = TesseraColorScheme(
    canvas = Color(0xFF2A1210),
    surface = Color(0xFF2A1F24),
    surfaceAlt = Color(0xFF3A2A30),
    ink = Color(0xFFFFF1E6),
    muted = Color(0xFFC9AFA2),
    faint = Color(0xFFA08D84),
    primary = Color(0xFFFF9E5E),
    primaryLight = Color(0xFFFFB98A),
    primaryDeep = Color(0xFFF2603C),
    teal = Color(0xFF4FDDB6),
    pink = Color(0xFFE0447E),
    purple = Color(0xFFB39BFF),
    gold = Color(0xFFFFC46B),
    hairline = Color(0x22FFF1E6),
    onPrimary = Color(0xFF2A1210),
)

val LocalTesseraColors = staticCompositionLocalOf { lightScheme }

/**
 * Backwards-compatible accessor. Names are kept from the previous design; each
 * now resolves to a v2 role so existing screens flip with the theme:
 * - Steel   → primary (coral)
 * - Paper   → surface
 * - Haze    → canvas
 * - Ink     → ink
 * - SplashBg→ canvas (dark hero uses gradients directly)
 */
object TesseraColors {
    val Ink: Color @Composable @ReadOnlyComposable get() = LocalTesseraColors.current.ink
    val Paper: Color @Composable @ReadOnlyComposable get() = LocalTesseraColors.current.surface
    val Haze: Color @Composable @ReadOnlyComposable get() = LocalTesseraColors.current.canvas
    val Steel: Color @Composable @ReadOnlyComposable get() = LocalTesseraColors.current.primary
    val SteelDeep: Color @Composable @ReadOnlyComposable get() = LocalTesseraColors.current.primaryDeep
    val Sky: Color @Composable @ReadOnlyComposable get() = LocalTesseraColors.current.primaryLight
    val Mist: Color @Composable @ReadOnlyComposable get() = LocalTesseraColors.current.surfaceAlt
    val Muted: Color @Composable @ReadOnlyComposable get() = LocalTesseraColors.current.muted
    val Faint: Color @Composable @ReadOnlyComposable get() = LocalTesseraColors.current.faint
    val Hairline: Color @Composable @ReadOnlyComposable get() = LocalTesseraColors.current.hairline
    val SplashBg: Color @Composable @ReadOnlyComposable get() = LocalTesseraColors.current.canvas

    // New v2 roles (used by restyled screens).
    val Primary: Color @Composable @ReadOnlyComposable get() = LocalTesseraColors.current.primary
    val PrimaryLight: Color @Composable @ReadOnlyComposable get() = LocalTesseraColors.current.primaryLight
    val Surface: Color @Composable @ReadOnlyComposable get() = LocalTesseraColors.current.surface
    val SurfaceAlt: Color @Composable @ReadOnlyComposable get() = LocalTesseraColors.current.surfaceAlt
    val Teal: Color @Composable @ReadOnlyComposable get() = LocalTesseraColors.current.teal
    val Pink: Color @Composable @ReadOnlyComposable get() = LocalTesseraColors.current.pink
    val Purple: Color @Composable @ReadOnlyComposable get() = LocalTesseraColors.current.purple
    val Gold: Color @Composable @ReadOnlyComposable get() = LocalTesseraColors.current.gold
    val OnPrimary: Color @Composable @ReadOnlyComposable get() = LocalTesseraColors.current.onPrimary
}
