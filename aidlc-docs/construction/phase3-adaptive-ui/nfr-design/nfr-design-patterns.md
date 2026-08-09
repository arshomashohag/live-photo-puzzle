# NFR Design Patterns — Phase 3 Adaptive UI

## Theming Pattern

### TP-1 Single source of truth theme
- `ThemeResolver.isDark(settings.theme, systemDark)` computes one boolean at the
  app root; `TesseraTheme(darkTheme)` provides the scheme once. Screens never
  branch on theme themselves.

### TP-2 Semantic color tokens
- `TesseraColors` exposes **role-based** colors resolved from the current theme
  (canvas/surface/onSurface/muted/faint/hairline/accent/accentDeep/sky/mist/
  splashBg) via a `LocalTesseraColors` composition local (or MaterialTheme
  extension). Screens use roles, not raw hex — one change flips both themes.
- Migration: audit each screen for hardcoded `TesseraColors.Paper/Ink/...` used
  as literal light values; replace with the theme-flipping role.

## Adaptive Pattern

### AP-1 WindowSizeClass → LayoutSpec
- `rememberWindowSize()` maps `WindowSizeClass` to COMPACT/MEDIUM/EXPANDED;
  `layoutSpec(size)` yields `{ maxContentWidthDp, gridColumns, boardMaxDp }`
  (pure, testable). Screens read the spec — no per-screen dp math.

### AP-2 Centered max-width container
- A `ContentContainer` composable applies `widthIn(max=spec.maxContentWidthDp)`
  and centers on wide screens; wraps each screen's content.

### AP-3 Capped board
- Board box = `Modifier.widthIn(max = spec.boardMaxDp).aspectRatio(1f)`,
  centered; tiles derive from the measured board size.

## Accessibility Patterns

### XP-1 Semantics at the touch target
- Icon-only/interactive composables carry `Modifier.semantics { contentDescription
  = … }`; board tiles compose position + state strings (extends Phase-1).

### XP-2 Minimum target size
- Interactive composables use `Modifier.sizeIn(minWidth = 48.dp, minHeight =
  48.dp)` (or `minimumInteractiveComponentSize`); audit taps that were smaller.

### XP-3 Contrast-safe roles
- Dark on-surface text `#E7ECF1` on canvas `#14202B` and accents (steel/sky) are
  chosen/verified for WCAG AA; documented in the token table.

### XP-4 Font-scaling-safe layout
- Replace clipping fixed `height(x)` on text-bearing rows with `heightIn(min=x)`
  / `wrapContentHeight`; `sp` for text (already), tested at max font scale.

## Reduced-Motion Pattern

### RM-1 Motion gate
- `rememberReducedMotion()` reads `Settings.Global.ANIMATOR_DURATION_SCALE == 0`.
  Splash/Generating/transition animations check it and render static/short when
  on.

## Performance Pattern

### PP-1 Hoisted theme state, scoped recomposition
- Theme boolean is a single hoisted `StateFlow`-derived value; only color
  provision recomposes on toggle, not business logic.

## Not Used (justified)
- No new resiliency/caching/network patterns — UI-only phase, no new failure
  surface.
