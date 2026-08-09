# Implementation Summary — Phase 3.5 v2 Redesign

Brownfield reskin. Kept `TesseraColors` accessor + primitive names stable so
screens flip to v2 automatically; applied per-level accents + drawer explicitly.

## Created
- `res/font/nunito_variable.ttf` (Nunito variable, weight axis)
- `domain/model/LevelPalette.kt` (LevelAccentKey, accentFor — pure)
- `ui/theme/Shapes.kt` (pill/card/cardLarge/chip/tile)
- `ui/theme/Elevation.kt` (softShadow/cardShadow/primaryGlow)
- `ui/screens/SettingsDrawerContent.kt` (drawer body; theme chips, toggles, reset)
- `test/domain/LevelPalettePropertiesTest.kt` (Kotest: totality/distinctness/determinism)

## Modified
- `ui/theme/Color.kt` — v2 `TesseraColorScheme` (light+dark warm palette); accessor
  names repointed to v2 roles + new roles (Primary/Teal/Pink/Purple/Gold/…).
- `ui/theme/Type.kt` — Nunito (variable, weight variations).
- `ui/theme/Theme.kt` — v2 schemes; `accentColor(key)`.
- `ui/theme/Primitives.kt` — v2 rounded/shadowed: RoundedCard (RegistrationFrame
  alias), PillButton (BlueprintButton alias), Chip, rounded DifficultyMeter,
  rounded GridPreview (accent param).
- `TesseraApp.kt` — `ModalNavigationDrawer` hosting SettingsDrawerContent; Home
  gear opens drawer; SETTINGS route removed.
- `HomeScreen.kt`, `DifficultyScreen.kt` — per-level accents on grid/meter.
- Other screens inherit v2 via the theme accessor (colors/shape/type flip).

## Removed
- `ui/screens/SettingsScreen.kt` (replaced by drawer).
- Barlow `.ttf` (×5) and blueprint registration-mark primitives.

## Requirement mapping
- FR35-1 theme → Color/Type/Theme/Shapes/Elevation.
- FR35-2 drawer → SettingsDrawerContent + ModalNavigationDrawer.
- FR35-3 reskin → primitives + accessor flip + accents.
- FR35-4 per-level → LevelPalette + accentColor (Home/Difficulty).
- FR35-6 Nunito → Type + font asset. FR35-7 cleanup → removals.
- PBT → LevelPalettePropertiesTest.

## Verification
- assembleDebug + testDebugUnitTest + lintDebug. Manual: v2 look light/dark,
  drawer, per-level colors, tablet, TalkBack, contrast.
