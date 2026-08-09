# Implementation Summary — Phase 3: Adaptive UI, Dark Theme, Accessibility

Brownfield changes. App code under `app/src/…`.

## Created
- `domain/model/ThemeResolver.kt` (pure isDark)
- `domain/model/LayoutSpec.kt` (WindowSize enum, LayoutSpec, layoutSpec)
- `ui/theme/WindowSize.kt` (rememberWindowSize, ContentContainer)
- `ui/theme/Motion.kt` (rememberReducedMotion)
- `presentation/SettingsViewModel.kt`
- `ui/screens/SettingsScreen.kt` (Theme selector, sound/haptics placeholders, Reset stats w/ confirm)
- `test/domain/ThemeLayoutPropertiesTest.kt` (Kotest PBT: theme truth-table + totality; layoutSpec monotonic)

## Modified
- `ui/theme/Color.kt` — `TesseraColorScheme` + `lightScheme`/`darkScheme` +
  `LocalTesseraColors`; `TesseraColors` is now a theme-resolving accessor (same
  public names → all 13 referencing files flip automatically).
- `ui/theme/Theme.kt` — `TesseraTheme(darkTheme)`; provides LocalTesseraColors +
  Material3 light/dark scheme.
- `TesseraApp.kt` — root reads Settings.theme + system-dark → ThemeResolver →
  TesseraTheme(darkTheme); SETTINGS route; Home onSettings.
- `HomeScreen.kt` — gear → Settings (a11y label); My-puzzles row; fixed heights →
  `heightIn(min=…)` for font-scaling safety.
- `BoardScreen.kt` — board capped at 560dp centered; header/pause aligned; tile
  semantics retained.
- `PuzzleSelectScreen.kt`, `MyPuzzlesScreen.kt` — grid columns from
  layoutSpec (2→3 on Expanded).
- `SplashScreen.kt` — reduced-motion gating.
- `data`: `StatsDao.deleteAll`, `StatsRepository.resetAll` (clears best_scores;
  keeps puzzles/boards — no schema change).

## Requirement / NFR mapping
- FR3-1 dark theme → ThemeResolver + Color/Theme + root wiring.
- FR3-2 Settings → SettingsScreen/VM.
- FR3-3 adaptive → WindowSize/layoutSpec/ContentContainer + grid columns.
- FR3-4 board cap → BoardScreen 560dp.
- FR3-5 reduced-motion → Motion + SplashScreen.
- NFR3-1 a11y → semantics (gear/settings), ≥48dp, heightIn font-scaling.
- PBT → ThemeLayoutPropertiesTest.

## Verification
- assembleDebug + testDebugUnitTest + lintDebug. Manual: light/dark, tablet
  columns, reduced-motion, TalkBack sweep, large font scale.
