# NFR Design Patterns — Phase 3.5 v2 Redesign

## Theming Patterns

### TP-1 Single-source v2 tokens
- Extend the Phase-3 `TesseraColorScheme` with v2 role values (light+dark);
  `LocalTesseraColors` provides them; `TesseraColors` accessor names unchanged so
  screens flip automatically. New roles added as accessor properties.
- `TesseraShapes` (pill/card/chip radii) and `TesseraElevation` (soft shadow
  specs) centralize shape/elevation — screens reference tokens, not literals.

### TP-2 Per-level accent indirection
- `LevelPalette.accentFor(difficulty)` (pure) → `LevelAccentKey`;
  `accentColor(key)` (@Composable) resolves to the themed color. Screens never
  hardcode a per-level hex — one place maps difficulty→accent, theme-aware.

## Primitive Patterns

### PP-1 Rounded, shadowed building blocks
- `PillButton`, `RoundedCard`, `Chip`, `Hero` encapsulate the v2 shape + shadow +
  Nunito styling. Screens compose these instead of raw `Box`+`background`.
- Soft shadow is a shared modifier/helper (consistent blur/offset/color) to keep
  cost and look uniform (P-1).

## Drawer Pattern

### DP-1 ModalNavigationDrawer + shared VM
- Home hosts a `ModalNavigationDrawer`; the drawer content is a
  `SettingsDrawerContent` composable driven by the reused `SettingsViewModel`.
  Open via menu icon; scrim/back closes. Replaces the Settings route (no separate
  destination).

## Accessibility Patterns

### XP-1 Contrast-verified token pairings
- Text/control color pairings are chosen against AA on both themes; a small doc
  table records the verified pairs. Where a v2 accent fails on a surface, use the
  `deep`/`ink` variant for text.

### XP-2 Semantics survive restyle
- Primitives forward `contentDescription`/semantics; ≥48dp min interactive size
  is baked into `PillButton`/icon buttons regardless of visual size.

### XP-3 Non-color status
- Per-level and progress keep labels/meters; color is additive only.

## Motion Pattern

### MP-1 Reduced-motion gate
- `bob`/`rise`/`pulse` helpers check `rememberReducedMotion()` → static/none.
  Decorative loops (bob) are cheap and pause under reduced-motion.

## Performance Pattern

### PF-1 Scoped recomposition + bounded shadows
- Token reads via composition local; animations scoped to their element; shadow
  blur bounded / tonal surfaces used on long lists to avoid overdraw.

## Not Used (justified)
- No resiliency/network/caching patterns — visual phase, no new failure surface.
