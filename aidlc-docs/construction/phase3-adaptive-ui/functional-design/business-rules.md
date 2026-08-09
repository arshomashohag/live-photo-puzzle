# Business Rules — Phase 3 Adaptive UI

## BR3-1 Theme resolution
- Effective dark = `ThemeResolver.isDark(settings.theme, systemInDark)`:
  SYSTEM→systemInDark, LIGHT→false, DARK→true.
- `TesseraTheme(darkTheme = effectiveDark)` supplies the color scheme app-wide;
  Compose recomposes on change (StateFlow).

## BR3-2 Dark palette (Q1=A, derived)
- Dark canvas `#14202B`, dark surface `#1D2D3D`, on-dark text `#E7ECF1`
  (near-paper), muted `#9DA9B4`, hairline = paper at low alpha.
- Accents retained: steel `#5980A6`, sky `#94BCE3`, mist used sparingly.
- Every screen uses semantic role tokens (canvas/surface/onSurface/accent/…)
  that resolve per theme — no fixed light-only hex remains.

## BR3-3 Adaptive layout (Q2=A, WindowSizeClass)
- Content max-width: COMPACT = full; MEDIUM/EXPANDED = **840 dp** centered.
- Grid columns:
  - Difficulty grid: 3 (all sizes).
  - Puzzle select & My puzzles: 2 on COMPACT/MEDIUM, **3 on EXPANDED**.
- Respect system bars / cutouts (`WindowInsets.safeDrawing`) and rotation; no
  hardcoded screen dimensions.

## BR3-4 Board sizing (Q3=A)
- Board is a centered square = `min(availableWidth, 560 dp)`. Phones fill width;
  tablets cap at 560 dp. Tiles derive from the board size; touch targets remain
  ≥ 48 dp (a 5×5 at 560 dp → ~112 dp tiles).

## BR3-5 Reduced motion (Q5 requirement)
- When the system "remove animations" setting is on:
  - Splash: show static icon, no rise/pulse; still exit ≤ 800 ms.
  - Generating: static blueprint, no pulse.
  - Screen transitions: default/none rather than custom motion.
- Detected via the platform animation-scale (Settings.Global ANIMATOR_DURATION_SCALE == 0).

## BR3-6 Settings behavior (Q4=A, Q7=A)
- Theme selector (System/Light/Dark) → `SettingsRepository.setTheme`; takes
  effect immediately.
- Sound / Haptics toggles are shown, wired to DataStore set*, labeled
  "Coming soon" (behavior lands Phase 4).
- **Reset statistics**: confirmation dialog → clears BestScore rows (solved
  counts + best times); **keeps** custom puzzles and saved boards.

## BR3-7 Accessibility conventions (NFR3-1)
- Every interactive element sets a `contentDescription` / semantic label.
- Board tiles: "Tile {n}, {selected|}, {swappable|}, {in place|}" (extends the
  Phase-1 semantics).
- Icon-only buttons (gear, back, shutter, delete) have descriptions.
- Touch targets ≥ 48 dp (audit and pad where needed).
- Text/element contrast meets WCAG AA on both themes (verify accents on dark).
- Layouts are font-scaling safe: replace clipping fixed heights with
  min-height/`wrapContentHeight`; test at largest font scale.
- Status not by color alone (meters + labels already; verify).

## BR3-8 No new permissions / offline
- Pure UI phase — no new permissions, no network. SECURITY-09 generic errors
  preserved; other security rules N/A.
