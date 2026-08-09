# Domain Entities — Phase 3.5 v2 Redesign

Reuses `Difficulty`, `ThemeMode`, `Settings`. New types are UI tokens; only the
per-level accent mapping is pure domain (PBT-able).

## Extended color scheme (`TesseraColorScheme`, v2 values)
Adds v2 role colors; keeps the accessor names so screens compile. New/retuned
roles: `canvas`, `surface`, `surfaceAlt`, `ink`, `muted`, `faint`, `primary`
(coral), `primaryLight`, `primaryDeep`, plus accent set `teal`, `pink`,
`purple`, `gold`. Light + dark instances per requirements token table.

## New tokens
- `TesseraShapes`: pill = 999.dp (RoundedCornerShape), card = 22.dp, cardLarge =
  28.dp, chip = 16.dp.
- `TesseraElevation`: soft colored shadow specs (e.g. primary glow) as helper
  Modifiers / values.

## Pure logic: LevelPalette
```
enum class LevelAccent(...) // teal, coral, purple resolved from scheme
object LevelPalette {
    fun accentFor(difficulty: Difficulty): LevelAccentKey
}
```
- EASY → TEAL, MEDIUM → CORAL, HARD → PURPLE. `LevelAccentKey` is a pure enum
  key; the actual Color is resolved from the theme scheme in the UI layer (so it
  flips light/dark). Total over all difficulties.

## v2 Primitives (ui/theme)
- `PillButton(text, onClick, kind = Primary/Secondary/Ghost, leadingIcon?)` —
  fully rounded, soft shadow, Nunito 800.
- `RoundedCard(modifier, content)` — 22dp radius, surface color, soft shadow.
- `Chip(text, accent)` — pill, accent-tinted background.
- `Hero(title, subtitle?, icon?)` — rounded header block with gradient/primary.

## Settings drawer state (presentation)
Reuses `SettingsViewModel`. Drawer open/close is UI state
(`ModalNavigationDrawer` or custom). Same actions: setTheme, setSound,
setHaptics, resetStats.

## Motion
`rememberReducedMotion()` (Phase 3) gates: splash icon `bob`, card `rise` on
entry, `pulse` on generating/loading.
