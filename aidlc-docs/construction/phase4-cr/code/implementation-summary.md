# Implementation Summary — Phase 4 CR

Four fixes: critical custom-image play bug, light-theme contrast, button
alignment, swipe-to-swap. Brownfield modify-in-place; design system preserved.

## Root cause (critical bug #3)
Custom (file-backed) puzzles imported and saved correctly, but the **play path
was never implemented** (deferred in Phase 2):
- `GameViewModel.toEnginePuzzle()` returned `null` for `ImageRef.FileRef`.
- `Puzzle` carried only a `@DrawableRes Int` — no file support.
- `ImageSlicer` decoded only from resources.
So `startBoard()` silently bailed → the user saw a read error. Not a URI/
permission issue (import used `contentResolver.openInputStream` correctly).

## A — File-backed play (fix)
- `Puzzle`: added `imagePath: String?` (bundled uses `imageRes`; exactly one set).
- `ImageSlicer`: added `slice(imagePath, gridSize)` — bounded `decodeFile`
  (inSampleSize→~1024px) + shared crop/grid; empty list on decode failure.
- `GameViewModel`: `FileRef` → file-backed `Puzzle` with `filesExist` guard;
  `loadTiles` routes drawable-vs-file; on missing file / decode failure sets a
  **recoverable** `BoardUiState.error` instead of a blank board.
- `BoardScreen`: `BoardErrorOverlay` shows the error with a "Go back" action.

## B — Contrast (#1)
Systemic cause: v2 repointed `SplashBg → canvas` (light in light theme), so
every hero surface rendered near-white text on cream.
- `PauseOverlay`: dark scrim + surface card + ink text (readable both themes).
- Hero surfaces now use a warm `heroGradient` (coral→pink) with white `OnHero`
  text: `SplashScreen`, `CompleteScreen`, `ImportGeneratingScreen`.
- `PuzzleSelect` RESUME badge, `Home` CTA: `Paper`→`OnPrimary` on coral.
- Themed both confirm dialogs via new `TesseraDialog` (surface/ink/accent).
- Darkened light-scheme `faint`/`muted` so caption text meets ~4.5:1.

## C — Alignment (#2)
- Drawer theme chips: `contentAlignment = Center` + `TextAlign.Center`.
- Standardized hand-rolled buttons to `PillButton` (centered by construction):
  Home CTA, create error/permission buttons, Complete buttons.

## D — Swipe-to-swap (#4)
- Engine: `Grid.neighborInDirection` + `Direction` enum; `BoardState.swipe(pos,
  dir)` reuses adjacent-swap (edge swipe = no-op). `GameViewModel.swipe()` with
  same completion/save handling as tap.
- Board: `pointerInput` flick/drag detection (dominant axis; ~24dp threshold;
  one move per gesture; ignored while animating). Tile slide via `graphicsLayer`
  translation animated 1→0 over 200ms `FastOutSlowIn`; honors reduced-motion.
- Engine rules, solvability, move counting, autosave all unchanged.

## E — Tests
- `SwipePropertiesTest` (Kotest PBT): swipe == equivalent two-tap; edge no-op;
  +1 move per valid swap. Instrumented `CustomPuzzleFlowTest`: imported image
  slices into gridSize² tiles for every difficulty; missing file → empty (no
  crash).
- **Verification**: `testDebugUnitTest` 40/40 pass; `lintDebug` 0 errors;
  `assembleDebug` OK (64MB debug APK).

## Known limitations
- Bitmap-decoding tests are instrumented (device-pending; no CI device), same as
  prior phases. Manual matrix (real camera, TalkBack, tablet) to verify on-device.
- Swipe is primary interaction; TalkBack users should use Explore-by-touch — a
  dedicated a11y swap action was not added this cycle (`tap` remains on the VM).
