# Tessera — Core Playable Vertical Slice (Design)

**Date:** 2026-08-09
**Status:** Approved for planning
**Source:** `Tessera Photo Puzzle.dc.html` UI/UX specification v1.0 (imported from Claude Design)

## Summary

Tessera is an Android photo-puzzle game built on a **swap-tile** mechanic. This
document specs the *core playable vertical slice*: the minimum set of screens
that make the app a real, installable, playable game — launch, browse, play,
win. Camera-create, library management, settings, dark theme, tablet layouts,
and edge-state screens are explicitly deferred (see "Out of Scope").

## Goals

- A returning-user-quality core loop: pick a difficulty, pick a puzzle, solve
  it by swapping tiles, see a completion screen.
- Faithful realization of the spec's "blueprint" visual language: square
  corners, hairline frames, `+` registration marks, steel duotone accent,
  Barlow Condensed / Barlow / monospace type.
- **Fully offline.** No network access at runtime, no `INTERNET` permission.

## Non-Goals (Out of Scope for this slice)

- Camera capture → puzzle generation flow.
- Library (My puzzles) management: saved grid, delete sheet.
- Settings screen.
- Dark theme.
- Tablet / adaptive (foldable, large-screen) layouts.
- Edge-state screens: permission denied, generation failed, first-launch
  onboarding, notices.
- Persistence of progress across app restarts (in-memory state only for the
  slice; the navigation graph leaves seams to add it later).

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose (Material3 as a base, heavily re-themed)
- **Navigation:** Navigation-Compose (single Activity)
- **Min SDK:** 26 (API 26+, per spec "TARGET API 26+")
- **Target/Compile SDK:** current stable (35+)
- **Build:** Gradle (Kotlin DSL), version catalog

## Screens (7)

| # | Screen        | Type            | Purpose |
|---|---------------|-----------------|---------|
| 1 | Splash        | Destination     | Android 12+ SplashScreen API; animated icon, ≤800ms, exits to Home. |
| 2 | Home          | Destination     | Returning-user home: Continue card (if a board is in progress), Create-from-camera CTA (visually present, routes to a "coming soon" no-op or is disabled for the slice), difficulty grid, My-puzzles row (disabled/stub), stats strip. |
| 3 | Difficulty    | Destination     | Three cards: Easy 3×3, Medium 4×4, Hard 5×5, each with name + grid preview + 1–3 bar meter. |
| 4 | Puzzle select | Destination     | Grid of the 3 bundled puzzles for the chosen difficulty; shows solved/played state. |
| 5 | Board         | Destination     | The swap-tile gameplay. |
| 6 | Pause         | Overlay on Board | Resume / Restart / Exit puzzle. |
| 7 | Complete      | Destination     | Full-screen win state with time + moves; Next / Home. |

### Navigation graph & back behaviour

```
Splash → Home
Home → Difficulty → PuzzleSelect → Board → Complete → (Home | PuzzleSelect)
Board: system BACK opens Pause (does not leave the board)
Pause → Resume (dismiss) | Restart (reshuffle same puzzle) | Exit (→ PuzzleSelect)
Complete: BACK returns to PuzzleSelect, never to a solved Board
```

The Continue card on Home resumes the last in-progress board (in-memory for the
slice); it is hidden when no board is in progress. The Create-from-camera CTA is
drawn per spec (the one solid accent object on Home) but is non-functional in
this slice: tapping it shows a brief inline "Coming soon" snackbar/label and does
not navigate.

## Design System (`ui/theme`)

Color tokens (light theme only for this slice):

- `ink` `#1D1F20`, `paper` `#F2F2F3`, `haze` `#E7E7EA` (canvas)
- `steel` `#5980A6` (primary accent), `steelDeep` `#2C455D`, `sky` `#94BCE3`,
  `mist` `#D6EBFF`
- `muted` `#5D5D60`, `faint` `#7A7A7D`, hairline `rgba(29,31,32,.28)`
- Duotone ramp for imagery/previews: `#D6EBFF → #94BCE3 → #749DC4 → #2C455D`

Type: Barlow (body), Barlow Condensed (display/headings, uppercase), a monospace
family for labels/registration text. Fonts bundled in `res/font` (Barlow +
Barlow Condensed from Google Fonts, OFL-licensed); monospace uses the system
monospace.

Reusable primitives:

- `RegistrationFrame` — bordered box (hairline) with `+` marks at corners; the
  signature container used across the spec.
- `BlueprintButton` — solid steel or outlined variants, square corners,
  Barlow Condensed uppercase label.
- `DifficultyMeter` — 1–3 filled bars.
- The mock's faux "9:41 + battery" header is **not** reproduced — the real app
  draws under the true system status bar and respects window insets. Screens lay
  out within `WindowInsets.safeDrawing` padding.
- `GridPreview` — the N×N line-grid thumbnail.

Accessibility: difficulty and progress are conveyed by name + shape + meter, not
color alone (per spec). All interactive cards are single touch targets ≥48dp.

## Domain Model (`model`)

```
enum Difficulty(gridSize: Int)          // EASY(3), MEDIUM(4), HARD(5)
    tileCount = gridSize * gridSize

data class Puzzle(
    id: String,
    name: String,             // e.g. "Ridgeline", "Harbour"
    imageRes: Int,            // bundled drawable
)

class BoardState(
    puzzle: Puzzle,
    difficulty: Difficulty,
    order: IntArray,          // current tile position → source tile index
    selected: Int?,           // currently selected tile position, or null
    moves: Int,
    elapsedMillis: Long,      // driven by a timer while board is active
) {
    fun tapTile(pos): BoardState      // select, or swap with selected
    val placedCount: Int              // tiles whose value == position
    val isSolved: Boolean             // order is identity
}
```

### Scramble generation (the critical logic)

- Produce a random permutation that is **not already solved** and is
  **reachable** — since swap-tile allows swapping *any* two tiles, every
  permutation is solvable, so no parity constraint is needed (unlike the classic
  15-puzzle). Guard only against the identity permutation and against a trivially
  near-solved start.
- Seedable RNG so tests are deterministic.

### Swap / solved logic

- `tapTile(pos)`: if nothing selected → select `pos`. If `pos` is already
  selected → deselect. Otherwise swap the two positions in `order`, clear
  selection, increment `moves`.
- `isSolved`: `order[i] == i` for all `i`.

## Image Pipeline (`data` / `PuzzleCatalog`) — Offline

**Build-time (development):** download 9 royalty-free photos from Picsum
(`https://picsum.photos/seed/<seed>/1024`) — Picsum serves Unsplash images under
a free-to-use license. Resize to ~1024px square, JPEG-compress (~quality 80),
and commit to `res/drawable-nodpi/`. Target ~1–1.5 MB total for all 9. A short
committed script (`tools/fetch_puzzle_images.sh`) records exactly which seeds map
to which puzzle so the set is reproducible.

**Runtime:** decode the bundled drawable once, center-crop to square, and slice
into an N×N grid of tile bitmaps. No network, no `INTERNET` permission. Decoding
is done off the main thread; sliced tiles are cached per (puzzle, difficulty)
while the board is active.

**Fallback:** if a drawable somehow fails to decode, tiles render as
duotone-gradient blocks (the spec's placeholder aesthetic) so the board is always
playable.

### Bundled puzzle catalog (3 per difficulty)

Names drawn from the spec ("Ridgeline", "Harbour", "Terrace", …). Every puzzle is
playable at any difficulty per the spec ("difficulty is a property of the board,
not the photo"), but for the slice each difficulty's Puzzle-select list shows a
fixed set of 3 to keep browse content concrete.

## Testing

- **Unit (JUnit, JVM — no device):**
  - Scramble is never the identity permutation and contains every index exactly
    once (valid permutation).
  - `tapTile` select → swap → deselect transitions behave correctly.
  - `placedCount` and `isSolved` compute correctly, including the fully-solved
    case.
  - Solving a scrambled board via a known sequence of swaps reaches `isSolved`.
- **Manual / build verification:** app builds, installs, and the core loop is
  playable (assembleDebug + lint). Instrumented UI tests are out of scope for the
  slice.

## Project Structure

```
settings.gradle.kts, build.gradle.kts, gradle/libs.versions.toml
app/
  build.gradle.kts
  src/main/
    AndroidManifest.xml                 // no INTERNET permission
    java/com/tessera/puzzle/
      MainActivity.kt
      TesseraApp.kt                     // NavHost + routes
      model/ (Difficulty, Puzzle, BoardState, Scramble)
      data/ (PuzzleCatalog, ImageSlicer)
      ui/theme/ (Color, Type, Theme, primitives/)
      ui/screens/ (Splash, Home, Difficulty, PuzzleSelect, Board, Pause, Complete)
    res/ (drawable-nodpi photos, font/, values/, mipmap launcher, splash)
  src/test/java/com/tessera/puzzle/     // unit tests
tools/fetch_puzzle_images.sh
```

## Risks / Open Questions

- **Font bundling:** Barlow / Barlow Condensed are OFL (free to bundle). Will
  commit the `.ttf` files under `res/font`.
- **Image licensing:** Picsum → Unsplash license permits free use including
  commercial; images are fetched once at build time, not at runtime.
- **No persistence:** progress is in-memory; a cold start resets Continue/stats.
  Acceptable for the slice; noted as the first natural extension.
