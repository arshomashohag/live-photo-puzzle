# Code Generation Plan — Phase 3.5: v2 Redesign

**Unit**: `phase35-redesign`
**Project type**: Brownfield (modify-in-place; remove v1 cruft).
**Code location**: `app/src/main/java/com/tessera/puzzle/…`; tests `app/src/test/…`.
Docs → `aidlc-docs/construction/phase35-redesign/code/`.

## Exact v2 visual reference (from the design)
- **Gradients**: primary `145deg #FF8A5B→#F2603C`; hero `160deg #FF9E5E→#F2603C→#E0447E`;
  level teal `150deg #7EE7C4→#17B892`, level gold `150deg #FFC46B→#FF8A2B`,
  level purple `150deg #B39BFF→#7C5CFF`.
- **Shadows**: card `0 8px 20px rgba(70,35,20,.08)`; primary glow
  `0 12px 26px rgba(242,96,60,.40)`; drawer `14px 0 40px rgba(70,35,20,.24)`.
- **Radii**: pill 999, card 22, cardLarge 28, chip 16.
- **Nunito** weights 600/700/800/900.

---

## Step 1: Nunito fonts + per-level pure logic + PBT
- [x] Add `res/font/nunito_regular|semibold|bold|extrabold|black.ttf`.
- [x] `domain/model/LevelPalette.kt` (`LevelAccentKey`, `accentFor`).
- [x] `test/.../domain/LevelPalettePropertiesTest.kt` (Kotest: totality + 3 distinct keys).

## Step 2: v2 color scheme + shapes + elevation + type
- [x] `ui/theme/Color.kt`: replace scheme values with v2 light+dark; add roles
  (primaryLight, primaryDeep, teal, pink, purple, gold, surfaceAlt) as scheme
  fields + accessor props. Keep existing names mapped to v2 (Steel→primary, etc.
  — or rename usages; simplest: repoint the existing accessor names to v2 roles).
- [x] `ui/theme/Shapes.kt` (TesseraShapes: pill/card/cardLarge/chip).
- [x] `ui/theme/Elevation.kt` (Modifier.softShadow(...), primaryGlow).
- [x] `ui/theme/Type.kt`: Nunito families + scale (display 900, heading 800,
  cardTitle 800, body 700/600, label 700, mono→Nunito 700).

## Step 3: TesseraTheme + accentColor
- [x] `ui/theme/Theme.kt`: v2 light/dark schemes into Material3 + LocalTesseraColors;
  `@Composable accentColor(key): Color`.

## Step 4: v2 primitives (replace blueprint)
- [x] `ui/theme/Primitives.kt`: `PillButton(text,onClick,kind,leadingIcon?)`,
  `RoundedCard`, `Chip(text,accent)`, `Hero(...)`, `GradientBox` helper. Remove
  `RegistrationFrame` + corner-mark bits and the old `BlueprintButton`
  (or alias BlueprintButton→PillButton to minimize call-site churn), `DifficultyMeter`
  restyled (rounded), `GridPreview` restyled (rounded tiles).

## Step 5: Restyle browse/home screens
NOTE: Because the `TesseraColors` accessor names + primitive names were kept
stable and repointed to v2, ALL screens flip to v2 colors/Nunito/rounded cards
automatically. Explicit per-screen touches done: Home + Difficulty per-level
accents; drawer trigger on Home. Splash/Select got the v2 look via the theme but
did NOT get bespoke gradient-icon/hero treatments — those refinements are
deferred (see Known Limitations).
- [x] `HomeScreen`: menu→drawer; difficulty cards use per-level accent; v2 theme.
- [x] `DifficultyScreen`: per-level accent on grid/meter; v2 theme.
- [~] `SplashScreen`, `PuzzleSelectScreen`: inherit v2 theme; bespoke
  gradient-icon / hero-gradient refinements deferred.

## Step 6: Restyle board/complete/create/library/states
- [~] Board/Complete/create/*/MyPuzzles/states: inherit v2 theme (rounded cards,
  Nunito, coral). Bespoke per-screen touches (accent ring on tiles, gradient
  generating, playful empty art) deferred — functional and on-brand via tokens.

## Step 7: Settings drawer (replaces Settings screen)
- [x] `ui/screens/SettingsDrawerContent.kt` (theme options as chips/rows,
  sound/haptics switches, reset-stats).
- [x] `TesseraApp.kt`: wrap Home in `ModalNavigationDrawer`; Home menu icon opens
  it; remove `Routes.SETTINGS` + `SettingsScreen` usage (delete SettingsScreen.kt).

## Step 8: Remove v1 cruft
- [x] Delete Barlow `.ttf`; delete unused blueprint primitives; fix refs.
- [x] Grep audit: no `barlow`, no `RegistrationFrame` remain.

## Step 9: Tests + build
- [x] LevelPalette PBT (Step 1) runs; existing suite unaffected.
- [x] `assembleDebug` + `testDebugUnitTest` + `lintDebug`.

## Step 10: Docs summary
- [x] `aidlc-docs/construction/phase35-redesign/code/implementation-summary.md`.

## Traceability
FR35-1 → Steps 2,3 · FR35-2 → Step 7 · FR35-3 → Steps 5,6 · FR35-4 → Steps 1,5,6 ·
FR35-5 → Steps 4,5 · FR35-6 → Steps 1,2 · FR35-7 → Step 8 · PBT → Step 1.

## Scope / Estimated
- 10 steps; ~6 files created + ~18 modified + deletions (Barlow, SettingsScreen,
  blueprint prims). Tests: LevelPalette PBT + existing suite.
