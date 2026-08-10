# Phase 4 CR — Design + Plan (Streamlined)

**Cycle**: `phase4-cr` — UI contrast, button alignment, critical custom-image
play bug, swipe-to-swap. Brownfield modify-in-place.

## Root causes (confirmed by code trace)

**#3 (critical)** — Custom (file-backed) puzzles never got a *play* path
(deferred in Phase 2):
- `GameViewModel.toEnginePuzzle()` returns `null` for `ImageRef.FileRef`.
- `domain/model/Puzzle.kt` carries only `@DrawableRes imageRes: Int`.
- `data/ImageSlicer.kt` decodes only from resources (`decodeResource`).
Import/save is fine (contentResolver.openInputStream is correct). Fix = finish
the file-backed play path end-to-end.

**#1** — `PauseOverlay` backdrop = `SplashBg`(=canvas, light in light theme);
text = `Paper`/`OnPrimary` (near-white) → unreadable in light theme. AlertDialog
uses default Material colors that clash with the warm palette.

**#2** — Drawer theme chips: `Box` has no `contentAlignment`, `Text` not
centered → label top-start.

**#4** — Board uses tap-to-swap. Keep adjacent-swap engine rules; change
interaction to directional flick with animated slide.

---

## Design

### #3 File-backed puzzle play (the real fix)
- Extend engine image reference so a `Puzzle` can be drawable OR file-backed.
  Minimal-churn approach: add `imagePath: String?` to `Puzzle` (keep
  `imageRes: Int` for bundled; exactly one is set). Engine stays pure (no
  Android types leak — path is just a String).
- `GameViewModel.toEnginePuzzle()`: `ImageRef.FileRef` → `Puzzle(id, name,
  imagePath = ref.imagePath)`. Guard: file exists (`PuzzleFileStore.filesExist`)
  else surface a recoverable error, not silent bail.
- `ImageSlicer`: add `slice(context, imagePath, gridSize)` that bounded-decodes
  the saved file (`BitmapFactory` with `inJustDecodeBounds` + `inSampleSize`
  sized to the on-screen board; images are already 1024² upright squares from
  import, so EXIF/crop already handled). Route in `loadTiles` by which field
  is set. Fail-safe: empty list on decode failure → tiles show placeholder,
  and startBoard reports error instead of a blank board.
- Because import already normalizes (EXIF-upright, center-crop square, 1024px),
  play-time slicing is simple and memory-bounded.

### #1 Contrast
- `PauseOverlay`: backdrop → a true scrim (`ink.copy(alpha=.72f)` over the
  board, theme-independent dark scrim) OR a `surface`-filled centered card with
  `ink` text. Choose: **surface card + ink text**, so it reads in both themes
  and matches v2 (rounded card, coral primary button). Buttons use `PillButton`
  defaults (OnPrimary on coral / Ink on surfaceAlt) — already high-contrast.
- Theme the `AlertDialog` (reset-stats) with `containerColor = surface`,
  `titleContentColor/textContentColor = ink`, button text = primary.
- Audit sweep: grep for `Paper`/`OnPrimary`/`Faint` used as text on light
  surfaces; verify `Faint` (#A08076 on #FFF6EF) meets ~4.5:1 for body — if a
  secondary text fails, bump to `Muted`. Board header secondary uses `Faint`;
  verify/adjust.

### #2 Button/label alignment
- Drawer theme chips: add `contentAlignment = Alignment.Center` to the Box and
  center the Text. Apply the same to any tap-target Box that renders a label.
- Confirm `PillButton` (already `Arrangement.Center` + `TextAlign.Center`),
  `DrawerToggle`, reset row. Standardize: any label-in-Box control centers.

### #4 Directional-flick swipe
- Replace `.clickable { game.tap(pos) }` with
  `pointerInput { detectDragGestures / detectHorizontalDrag+vertical }` OR
  `awaitPointerEventScope` tracking total drag + velocity. On gesture end,
  pick dominant axis+direction; map to neighbor via `Grid.neighbors`; if a
  neighbor exists in that direction, call a new `game.swipe(pos, direction)`.
- Thresholds: register when drag distance > ~1/2 tile OR flick velocity >
  threshold; below both = ignore (no accidental micro-moves). One move per
  gesture (consume; ignore further drags until pointer up).
- Animation: track an `animating` flag; while animating, ignore new gestures
  (CR: disable conflicting interactions). Animate the two swapped tiles'
  offsets with `Animatable`/`animateOffsetAsState`, ~180–220ms, `FastOutSlowIn`.
  Board responsive immediately after settle.
- Engine unchanged: `game.swipe` computes the neighbor position for the
  direction and reuses the existing adjacent-swap (same as two taps), so rules,
  solvability, move counting, autosave all preserved.
- Accessibility: keep tap-to-select-then-tap as a fallback for TalkBack
  (custom actions), so the game stays operable without gestures. (Minimal:
  retain `tap` on the VM; add semantics swipe actions.)

---

## Plan (checkboxes)

### A. Critical image bug (#3)
- [ ] A1 `Puzzle`: add `imagePath: String?` (default null); keep `imageRes`.
- [ ] A2 `ImageSlicer`: add file-path bounded-decode `slice` overload; shared
  square-crop+grid logic.
- [ ] A3 `GameViewModel.toEnginePuzzle`: map `FileRef`→file-backed Puzzle with
  file-exists guard; `loadTiles` routes drawable vs file.
- [ ] A4 startBoard: on missing file / decode fail, surface recoverable error
  state (not silent return) — add a board error signal the screen shows.
- [ ] A5 Verify import→save→play for gallery + camera, 3×3/4×4/5×5,
  portrait/landscape/square, hi-res; app restart after save.

### B. Contrast (#1)
- [ ] B1 `PauseOverlay` → surface card + ink text, dark scrim behind.
- [ ] B2 Theme the reset `AlertDialog` (surface/ink/primary).
- [ ] B3 Contrast audit sweep across screens; fix any near-white-on-light /
  faint-secondary failures; keep design system.

### C. Alignment (#2)
- [ ] C1 Drawer theme chips centered (contentAlignment + Text center).
- [ ] C2 Standardize label-in-Box controls; verify PillButton/toggles/reset.
- [ ] C3 Spot-check small/large phone + tablet + font-scale via preview/layout.

### D. Swipe-to-swap (#4)
- [ ] D1 Engine helper: `game.swipe(pos, Direction)` → neighbor swap reusing
  adjacent-swap; pure `Grid` direction→neighbor mapping (+ PBT: swipe == the
  equivalent two-tap for a valid neighbor; edge swipe = no-op).
- [ ] D2 Board gesture: `pointerInput` flick/drag detection, thresholds,
  one-move-per-gesture, ignore-while-animating.
- [ ] D3 Tile slide animation (~200ms FastOutSlowIn), no flicker/jump.
- [ ] D4 A11y fallback (semantics/select-tap retained).

### E. Tests + build
- [ ] E1 Regression tests: file-slice path (decode a temp JPEG → N² tiles);
  swipe==two-tap PBT + edge no-op; existing suite unaffected.
- [ ] E2 `assembleDebug` + `testDebugUnitTest` + `lintDebug` green.
- [ ] E3 Implementation summary; update aidlc-state.md.

## Traceability
CR#1→B · CR#2→C · CR#3→A · CR#4→D · CR#5(regression)→E.

## Known limits (to disclose)
- Instrumented/device tests remain device-pending (no CI device); manual matrix
  (real camera capture, TalkBack, tablet) verified by user on-device.
