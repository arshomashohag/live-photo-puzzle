# Domain Entities — Phase 3 Adaptive UI

Reuses `Settings.theme` (`ThemeMode`) from Phase 1. New types are UI-adjacent but
the resolver is pure (domain).

## Reused
- `ThemeMode { SYSTEM, LIGHT, DARK }` (Phase 1, DataStore-backed).
- `SettingsRepository` (get/set theme, sound, haptics).

## New pure logic: ThemeResolver
```
object ThemeResolver {
    // Effective dark? given the user's ThemeMode and whether the OS is in dark.
    fun isDark(mode: ThemeMode, systemInDark: Boolean): Boolean
}
```
- SYSTEM → `systemInDark`
- LIGHT → false
- DARK → true

## New value: WindowSize (adaptive)
Derived from Material3 `WindowSizeClass`; kept as a small helper.
| Bucket | Meaning |
|---|---|
| COMPACT | phones (portrait) |
| MEDIUM | large phones / small tablets / landscape |
| EXPANDED | tablets / large screens |

Adaptive parameters (see business-rules):
- content max-width, grid column count, board max size.

## Color tokens (both themes)
`TesseraColors` gains dark variants. Screens read semantic roles that flip with
theme rather than fixed hex. Roles:
- `canvas` (haze/dark canvas), `surface` (paper/dark surface),
  `onSurface` (ink/paper), `muted`, `faint`, `hairline`,
  `accent` (steel), `accentDeep`, `sky`, `mist`, `splashBg`.

## Settings screen state (presentation)
`SettingsUiState(theme: ThemeMode, soundEnabled, hapticsEnabled)` from
`SettingsRepository.settings` (StateFlow). Actions: setTheme, (placeholder)
setSound/setHaptics, resetStats (with confirm).
