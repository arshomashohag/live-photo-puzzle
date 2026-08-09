# Requirements — Phase 3: Adaptive UI, Dark Theme, Accessibility

## Intent Analysis
- **Request**: Make the UI production-grade across light/dark themes, phone/tablet
  sizes, and accessibility.
- **Type**: Enhancement (UI/UX hardening) on the existing Compose app.
- **Scope**: Cross-cutting UI (theme, all screens, board sizing) — no new
  gameplay/data.
- **Complexity**: Moderate.
- **Depth**: Standard–Comprehensive.

## Clarifying Answers (all recommended, "A")
- **Q1=A** Dark theme: follow **system by default** with a **Settings override**
  (System/Light/Dark) via the existing DataStore `theme` field.
- **Q2=A** **No dynamic color** — keep Tessera's steel-blue blueprint identity
  consistent across devices.
- **Q3=A** **Adaptive layouts via WindowSizeClass** — wider max-width,
  multi-column where sensible, single codebase.
- **Q4=A** **Cap + center the board** on large screens; scale tiles/targets.
- **Q5=A** **Respect system reduced-motion** — skip/shorten splash, generating,
  and transition animations when enabled.
- **Q6=A** **Full accessibility pass** — content descriptions, tile semantics,
  focus order, ≥48dp targets, contrast (both themes), font-scaling safe layouts,
  non-color-only status.
- **Q7=A** **Build the Settings screen now** with the Theme control; sound/haptics
  toggles present but noted "Phase 4" (wired to DataStore).

## Functional Requirements

### FR3-1 Dark theme
- A complete dark color scheme (the design doc's dark palette): dark canvas,
  paper→ink inversion, steel/sky accents legible on dark.
- Theme resolves from `Settings.theme` (SYSTEM → follow device; LIGHT; DARK).
- All screens render correctly in both themes (no hardcoded light-only colors).

### FR3-2 Settings screen
- New Settings screen reachable from Home (gear icon already present).
- **Theme** selector (System / Light / Dark) → `SettingsRepository.setTheme`.
- Sound / Haptics toggles shown, wired to DataStore, labeled "Coming in a later
  update" (functional wiring lands in Phase 4).
- "Reset stats" action (optional) — clears best scores (confirm first).

### FR3-3 Adaptive layouts
- Use `WindowSizeClass` (Compact / Medium / Expanded).
- Content max-width on Expanded; difficulty/puzzle grids use more columns on
  wider screens; larger type scale where appropriate.
- Respect system bars, display cutouts, and rotation; no hardcoded screen
  dimensions.

### FR3-4 Board sizing
- Board is a centered square capped at a comfortable max (e.g. ≤ 560 dp) on
  large screens; fills width on phones. Tiles and touch targets scale with the
  board.

### FR3-5 Reduced motion
- When the system "remove animations" setting is on: splash icon animation,
  Generating pulse, and screen transitions are skipped or reduced.

## Non-Functional Requirements

### NFR3-1 Accessibility (the core of this phase)
- Every interactive element has a content description / semantic label.
- Board tiles expose position + state ("Tile 3, selected, swappable, in place").
- Logical focus order; no keyboard/TalkBack traps.
- All touch targets ≥ 48 dp.
- Contrast meets WCAG AA for text on both themes (verify accents on dark).
- Layouts are **font-scaling safe** — no clipped text at large font sizes
  (remove fixed heights that clip; use min-height/wrap).
- Status never conveyed by color alone (meters, labels already help; verify).
- Reduced-motion honored (FR3-5).

### NFR3-2 Consistency / maintainability
- Colors come from the theme (`MaterialTheme`/`TesseraColors` light+dark
  variants) — no per-screen hardcoded hex that breaks dark mode.
- Theme + size logic centralized (a `TesseraTheme(darkTheme=…)` and a window-size
  helper), not duplicated per screen.

### NFR3-3 Security / Privacy / Offline
- Unchanged: offline, no new permissions, no network. (Security rules largely
  N/A to a UI phase; SECURITY-09 generic errors still honored.)

### NFR3-4 Testing
- **Unit/PBT**: theme resolution logic (SYSTEM/LIGHT/DARK → effective dark
  boolean) as a pure function; window-size bucket mapping if extracted as pure.
- **Instrumented/Compose UI**: key screens render in light + dark; Settings theme
  toggle changes theme; a couple of semantics assertions.
- **Manual device matrix**: small/standard/large phone, tablet, font scales
  (up to largest), light/dark, reduced-motion, TalkBack sweep of core flow.

## Out of Scope
- Audio/haptics behavior (Phase 4), release/AAB/signing (Phase 5), docs &
  compliance (Phase 6). No new gameplay or data.

## Key Requirements Summary
Phase 3 makes Tessera **theme-aware (system + Settings override, no dynamic
color)**, **adaptive across phone/tablet via WindowSizeClass with a capped
centered board**, **reduced-motion aware**, and **accessible** (content
descriptions, tile semantics, focus order, ≥48dp targets, AA contrast on both
themes, font-scaling-safe layouts). Adds a **Settings screen** with a Theme
control (sound/haptics placeholders for Phase 4). Pure theme-resolution logic is
property/unit-tested; screens get light/dark Compose UI checks.
