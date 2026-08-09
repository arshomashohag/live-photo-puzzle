# Business Rules — Phase 3.5 v2 Redesign

## BR35-1 v2 token scheme
- `TesseraColorScheme` holds v2 light + dark values (requirements token table).
- `TesseraColors` accessor (Phase-3 pattern) keeps its public names so all
  screens flip automatically; new roles (primaryLight/deep, teal/pink/purple/
  gold, surfaceAlt) added.
- Shapes/elevation come from `TesseraShapes`/`TesseraElevation`, not literals.

## BR35-2 Per-level accent (Q2=A)
- `LevelPalette.accentFor`: EASY→TEAL, MEDIUM→CORAL, HARD→PURPLE. Total,
  deterministic. UI resolves the key to a themed Color.
- Applied to: difficulty cards, puzzle-select header, board accent (selected-tile
  ring / progress), complete screen. Never the sole state signal (labels/meters
  retained — A11y).

## BR35-3 Rounded, shadowed primitives
- Buttons are pills (999dp); cards are 22–28dp rounded with soft colored shadow;
  chips are pill-tinted. Replace v1 square frames + registration marks.

## BR35-4 Settings drawer (Q1=A)
- Left slide-out drawer opened from Home (menu icon). Contains Theme
  (System/Light/Dark), Sound/Haptics placeholder toggles, Reset statistics
  (confirm dialog). Closes on scrim tap / back / selection where appropriate.
- The standalone Settings route/screen is removed; its controls move into the
  drawer (same `SettingsViewModel`).

## BR35-5 Motion (Q4=A) — reduced-motion gated
- Splash icon `bob` loop (or static if reduced-motion); list cards `rise` in on
  first show; generating/loading uses `pulse`/`spin`. All check
  `rememberReducedMotion()` → static/none when on.

## BR35-6 Typography (Q5=A)
- Nunito bundled (`res/font`), weights 400/600/700/800/900. `TesseraType`
  restyled to Nunito scale (display 900, heading 800, body 600/700, label 700).

## BR35-7 Cleanup (Q6=A)
- Remove Barlow `.ttf` and blueprint registration-mark primitives once unused.
  Build must stay green (no dangling references).

## BR35-8 Accessibility preserved (NFR35-1)
- All Phase-3 semantics/targets/font-scaling retained through the reskin.
- Verify WCAG AA contrast for text/controls on both v2 themes; adjust a token if
  a pairing fails (documented).

## BR35-9 No regressions
- Architecture, gameplay (adjacent-swap), persistence, existing tests unchanged;
  app builds; lint 0 errors.
