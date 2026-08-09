# Code Generation Plan — Phase 3: Adaptive UI, Dark Theme, Accessibility

**Unit**: `phase3-adaptive-ui`
**Project type**: Brownfield (modify-in-place).
**Code location**: `app/src/main/java/com/tessera/puzzle/…`; tests in
`app/src/test/…`, `app/src/androidTest/…`. Docs → `aidlc-docs/construction/
phase3-adaptive-ui/code/`.

## Key design decision — theme-aware tokens with minimal churn
`TesseraColors` becomes **theme-resolving** instead of a hardcoded object, so the
13 files already referencing `TesseraColors.X` flip automatically with the theme:
- Introduce a `TesseraColorScheme` data class (all role colors) with `lightScheme`
  and `darkScheme` instances.
- Provide it via `LocalTesseraColors` (a `staticCompositionLocalOf`).
- Keep `TesseraColors` as a `@Composable`-readable accessor object whose `val`s
  read `LocalTesseraColors.current` — so existing `TesseraColors.Steel` etc. keep
  working but now resolve per theme. (Where a value is read outside a composable,
  refactor that call site.)

This avoids touching every screen line-by-line for colors; screens change mainly
for adaptive layout + accessibility.

---

## Step 1: Pure ThemeResolver + tests
- [x] `domain/model/ThemeResolver.kt` (`isDark(mode, systemInDark)`).
- [x] `test/.../domain/ThemeResolverTest.kt` (Kotest PBT: truth table, totality).

## Step 2: Theme scheme (light + dark) + composition local
- [x] Refactor `ui/theme/Color.kt`: add `data class TesseraColorScheme(...)`,
  `lightScheme`, `darkScheme` (dark tokens per BR3-2), `LocalTesseraColors`, and
  a `TesseraColors` accessor object reading the local. Keep the same public
  names (Steel, Paper, Ink, Haze, …) mapped to roles so existing refs compile.
- [x] Where `TesseraColors.X` is used at file/top level (non-composable), adjust.

## Step 3: TesseraTheme(darkTheme) wiring
- [x] `ui/theme/Theme.kt`: `TesseraTheme(darkTheme: Boolean, content)` selects
  light/dark Material3 scheme + provides `LocalTesseraColors`.
- [x] App root (`TesseraApp`): inject/collect settings theme + `isSystemInDarkTheme()`
  → `ThemeResolver.isDark` → `TesseraTheme(darkTheme=…)`. Needs a settings source
  at root — obtain via a small `@HiltViewModel ThemeHolderViewModel` exposing
  `Settings` StateFlow, or read from `SettingsViewModel`.

## Step 4: Window size + layout spec (+ PBT)
- [x] `ui/theme/WindowSize.kt`: `WindowSize` enum, `rememberWindowSize()`
  (Material3 `calculateWindowSizeClass`/`currentWindowAdaptiveInfo`), `LayoutSpec`,
  `layoutSpec(size)`, and `ContentContainer` (centered max-width).
- [x] `test/.../domain/LayoutSpecTest.kt` (Kotest: monotonic columns, constants).
  (Keep `layoutSpec` pure in domain if Android-free; else example test.)

## Step 5: Reduced-motion helper
- [x] `ui/theme/Motion.kt`: `rememberReducedMotion()` (animator duration scale).

## Step 6: Dark palette + apply theme wiring; adaptive containers on browse screens
- [x] Wrap Home / Difficulty / PuzzleSelect / MyPuzzles content in
  `ContentContainer`; set grid columns from `layoutSpec` (2→3 on EXPANDED).
- [x] Splash + Generating: gate animations with `rememberReducedMotion()`.

## Step 7: Board capping + tile semantics
- [x] `BoardScreen`: board `widthIn(max = spec.boardMaxDp)` centered; keep tile
  semantics (extend with position/state per BR3-7).

## Step 8: Accessibility pass
- [x] Add `contentDescription` to icon-only controls (Home gear/back arrows,
  camera shutter already, delete already, chooser buttons already). Audit for
  ≥48dp (`minimumInteractiveComponentSize` / `sizeIn`). Replace clipping fixed
  `height(x)` on text rows with `heightIn(min=x)` where risk of clipping.

## Step 9: Settings screen + SettingsViewModel + resetStats
- [x] `data`: add `StatsRepository.resetAll()` + DAO `DELETE FROM best_scores`.
- [x] `presentation/SettingsViewModel.kt` (@HiltViewModel): settings StateFlow;
  setTheme/setSound/setHaptics/resetStats.
- [x] `ui/screens/SettingsScreen.kt`: Theme selector (System/Light/Dark),
  sound/haptics placeholder toggles ("Coming soon"), Reset statistics (confirm).
- [x] Wire Home gear → Settings route; add `Routes.SETTINGS`.

## Step 10: Compose UI + unit tests
- [x] `test`: ThemeResolver + layoutSpec PBT (ThemeLayoutPropertiesTest — done).
- [ ] `androidTest/.../ThemeUiTest.kt` — NOT created (was optional/device-only);
  light/dark render verified manually on device instead. Deferred.

## Step 11: Docs summary
- [x] `aidlc-docs/construction/phase3-adaptive-ui/code/implementation-summary.md`.

## Traceability
FR3-1 → Steps 1,2,3 · FR3-2 → Step 9 · FR3-3 → Steps 4,6 · FR3-4 → Step 7 ·
FR3-5 → Step 5,6 · NFR3-1 (a11y) → Steps 7,8 · PBT → Steps 1,4.

## Scope / Estimated
- 11 steps; ~6 files created + ~14 modified. Tests: ThemeResolver + layoutSpec
  PBT; optional Compose UI (device). Manual a11y/tablet/dark sweep noted.
