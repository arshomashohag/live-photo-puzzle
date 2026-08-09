# Business Logic Model — Phase 3.5 v2 Redesign

## Pure logic (domain — PBT-able)
```
enum class LevelAccentKey { TEAL, CORAL, PURPLE }
object LevelPalette {
    fun accentFor(difficulty: Difficulty): LevelAccentKey  // EASY→TEAL, MEDIUM→CORAL, HARD→PURPLE
}
```
No Android types.

## Theme system (ui/theme)
- `TesseraColorScheme` (data class, v2 roles) + `lightScheme`/`darkScheme`
  (v2 values) — extends the Phase-3 structure.
- `TesseraColors` accessor unchanged in shape (reads `LocalTesseraColors`).
- `accentColor(key): Color` @Composable — resolves LevelAccentKey to the themed
  color (teal/coral/purple per scheme).
- `TesseraShapes` (pill/card/chip radii), `TesseraElevation` (soft shadows).
- `TesseraType` → Nunito scale.

## Primitives (ui/theme/Primitives.kt — replaced)
`PillButton`, `RoundedCard`, `Chip`, `Hero` (rounded + shadow, Nunito). Old
`RegistrationFrame`/blueprint corner-mark primitives removed; call sites migrate
to `RoundedCard`.

## Drawer (ui)
`ModalNavigationDrawer` (Material3) hosting the settings content, opened from
Home via a menu icon; `SettingsViewModel` reused. Replaces `Routes.SETTINGS`.

## Data Flows
- **DF35-1 theme**: unchanged resolution (ThemeResolver) → v2 scheme applied.
- **DF35-2 per-level**: screen reads `LevelPalette.accentFor(difficulty)` →
  `accentColor(key)` → applies to card/board/meter.
- **DF35-3 drawer**: Home menu → open drawer → SettingsViewModel actions.
- **DF35-4 motion**: reduced-motion gate → animate or static.

---

## Testable Properties (PBT-01) — MANDATORY
Framework: Kotest. Target = pure `LevelPalette`.

| Property | Category | Statement |
|---|---|---|
| Accent totality | Invariant | `accentFor` defined for every `Difficulty`, no exceptions. |
| Accent determinism/distinctness | Invariant | `accentFor` is a function (same input→same key); the three difficulties map to three distinct keys (TEAL/CORAL/PURPLE). |

### No PBT
- Compose theming, primitives, drawer, motion, screen rendering — Android/UI;
  covered by build + manual visual/a11y sweep. (Existing engine/persistence/image
  PBT unchanged.)
