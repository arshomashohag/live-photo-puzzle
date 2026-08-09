# Logical Components — Phase 3.5 v2 Redesign

## Component map
```
domain/model/
  LevelPalette (pure accentFor) — PBT
  LevelAccentKey (enum)

ui/theme/
  Color.kt      (v2 TesseraColorScheme light+dark; extended roles; LocalTesseraColors)
  Type.kt       (Nunito families + scale)
  Shapes.kt     (TesseraShapes: pill/card/chip radii) [new]
  Elevation.kt  (TesseraElevation: soft shadow helpers) [new]
  Theme.kt      (TesseraTheme(darkTheme) — v2 schemes; accentColor(key))
  Primitives.kt (PillButton, RoundedCard, Chip, Hero — replaces blueprint prims)
  (removed) blueprint RegistrationFrame corner-mark styling

ui/screens/
  SettingsDrawerContent [new]  — drawer body, reuses SettingsViewModel
  (modified) TesseraApp — Home hosts ModalNavigationDrawer; SETTINGS route removed
  (restyled) Splash, Home, Difficulty, PuzzleSelect, Board(+Pause/Complete),
    MyPuzzles, create/* , states — v2 primitives/tokens, per-level accents, motion

res/font/
  nunito_* .ttf [new]; barlow_* removed

presentation/
  SettingsViewModel (reused, unchanged)
```

## Responsibilities
| Component | Responsibility | NFR ties |
|---|---|---|
| LevelPalette | difficulty → accent key (pure) | TP-2, PBT |
| Color/Type/Shapes/Elevation | v2 tokens single source | TP-1 |
| Primitives | rounded+shadow building blocks | PP-1, XP-2 |
| SettingsDrawerContent + ModalNavigationDrawer | settings via drawer | DP-1 |
| accentColor | resolve level key to themed color | TP-2 |
| screens (restyled) | apply tokens/primitives/accents/motion | XP-1..3, MP-1 |

## Integration
- Same UDF/StateFlow architecture; theming via composition local; drawer state
  local to Home; no servers/network. All on-device.
