# Business Logic Model — Phase 3 Adaptive UI

## Pure logic (domain — PBT-able)
```
object ThemeResolver {
    fun isDark(mode: ThemeMode, systemInDark: Boolean): Boolean
}
```
No Android types → JVM/property testable.

## Theme wiring
```
@Composable TesseraTheme(darkTheme: Boolean, content)  // picks light/dark scheme
// App root:
val settings by settingsRepository.settings.collectAsStateWithLifecycle()
val systemDark = isSystemInDarkTheme()
TesseraTheme(darkTheme = ThemeResolver.isDark(settings.theme, systemDark)) { ... }
```

## Adaptive helper
```
enum class WindowSize { COMPACT, MEDIUM, EXPANDED }
@Composable fun rememberWindowSize(): WindowSize   // from WindowSizeClass
data class LayoutSpec(val maxContentWidthDp: Int, val gridColumns: Int, val boardMaxDp: Int)
fun layoutSpec(size: WindowSize): LayoutSpec        // pure mapping (BR3-3/4)
```

## Presentation
```
@HiltViewModel SettingsViewModel:
  val state: StateFlow<SettingsUiState>   // theme, sound, haptics
  fun setTheme(mode); fun setSound(b); fun setHaptics(b)
  fun resetStats()                        // clears BestScore rows
```
Reuses `SettingsRepository`; `resetStats` via a new `StatsRepository.resetAll()`
(clears best_scores table; keeps puzzles/boards).

## Key Data Flows
- **DF3-1 theme**: DataStore theme + system-dark → ThemeResolver → TesseraTheme →
  all screens recolor.
- **DF3-2 adaptive**: WindowSizeClass → LayoutSpec → screens apply max-width /
  columns / board cap.
- **DF3-3 settings**: SettingsScreen ↔ SettingsViewModel ↔ SettingsRepository;
  resetStats → StatsRepository.resetAll (confirm first).
- **DF3-4 reduced-motion**: read animator scale → gate animations.

---

## Testable Properties (PBT-01) — MANDATORY

Framework: Kotest (seeded). Target = pure `ThemeResolver` (+ `layoutSpec` if kept
pure).

| Property | Category | Statement |
|---|---|---|
| Theme resolution truth table | Invariant | `isDark(LIGHT, *) == false`; `isDark(DARK, *) == true`; `isDark(SYSTEM, s) == s` for all s. |
| Theme resolution total | Invariant | Defined for all (mode, systemInDark) with no exceptions. |
| LayoutSpec monotonic columns | Invariant | `layoutSpec(EXPANDED).gridColumns >= layoutSpec(COMPACT).gridColumns`; boardMaxDp constant (560); maxContentWidth finite. |

### No PBT
- Compose theming, Settings UI, adaptive rendering, reduced-motion detection —
  Android/side-effectful; covered by Compose UI + manual tests.
