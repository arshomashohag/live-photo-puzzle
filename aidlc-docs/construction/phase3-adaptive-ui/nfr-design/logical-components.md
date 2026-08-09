# Logical Components — Phase 3 Adaptive UI

## Component map
```
domain/model/
  ThemeResolver (pure isDark) — PBT
  WindowSize / LayoutSpec (pure layoutSpec mapping) — PBT

ui/theme/
  Color.kt        (+ dark scheme; role tokens for both themes)
  Theme.kt        (TesseraTheme(darkTheme); LocalTesseraColors)
  WindowSize.kt   (rememberWindowSize, layoutSpec, ContentContainer)
  Motion.kt       (rememberReducedMotion)

presentation/
  SettingsViewModel (@HiltViewModel) — StateFlow<SettingsUiState>; setTheme/
    setSound/setHaptics/resetStats

ui/screens/
  SettingsScreen (new) — Theme selector, sound/haptics placeholders, reset-stats
  (modified) Splash, Home, Difficulty, PuzzleSelect, Board, Complete,
    MyPuzzles, create/* — theme roles, adaptive spec, semantics, targets,
    font-scaling-safe, reduced-motion

app root (TesseraApp / MainActivity)
  collect Settings.theme + isSystemInDarkTheme → TesseraTheme(darkTheme)

data/
  StatsRepository.resetAll() (new) — clears best_scores; keeps puzzles/boards
  (reused) SettingsRepository (theme/sound/haptics)
```

## Responsibilities
| Component | Responsibility | NFR ties |
|---|---|---|
| ThemeResolver | mode+systemDark → dark boolean | TP-1, PBT |
| Theme.kt / LocalTesseraColors | provide role colors per theme | TP-2 |
| WindowSize/layoutSpec | size bucket → layout params | AP-1, PBT |
| ContentContainer | centered max-width wrapper | AP-2 |
| rememberReducedMotion | motion gate | RM-1 |
| SettingsViewModel + SettingsScreen | theme control, reset-stats | DF3-3 |
| StatsRepository.resetAll | clear best scores | DF3-3 |
| screens (modified) | apply tokens/spec/semantics/targets | XP-1..XP-4, AP-3 |

## Integration
- UDF: theme + settings via StateFlow; adaptive via composition-local window size.
  No servers/network. All on-device.
