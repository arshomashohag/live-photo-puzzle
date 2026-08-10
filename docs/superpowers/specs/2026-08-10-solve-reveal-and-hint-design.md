# Solve Reveal + Hint — Design

**Date:** 2026-08-10
**Status:** Approved (design), pending implementation plan
**Scope:** Two gameplay features on the puzzle board — a full-image reveal on
solve, and a limited-use Hint that peeks at the full image mid-game.

## Goal

1. **Solve reveal:** When a puzzle is solved, hold the board for ~2s showing the
   seamless full image (no tile borders, no highlights) before transitioning to
   the Complete screen — the "it's whole now" payoff.
2. **Hint:** A `Hint (n)` button below the board. Tapping it overlays the full
   original photo on top of the scrambled board for ~2.5s, then fades back. The
   board state is untouched and **the timer keeps running** during the hint.
   Limited to 3 per game; the button disables at 0; resets on new board /
   Restart.

## Non-goals

- No change to the Complete screen (it stays a separate stats screen).
- No persistence of hint count (boards are not persisted by design).
- No change to swap/solve engine rules.

## Constants

| Name | Value | Where |
|------|-------|-------|
| `REVEAL_HOLD_MS` | 2000 | `BoardScreen.kt` |
| `HINT_MS` | 2500 | `BoardScreen.kt` |
| `HINT_COUNT` | 3 | `GameViewModel` (start/reset value) |
| `HINT_FADE_MS` | ~200 | `BoardScreen.kt` (overlay fade in/out) |

## Feature 1 — Solve reveal on the board

At the instant the board becomes solved, `board.order` is the identity
permutation, so the existing grid already renders every tile in its correct
place — i.e. the whole image, modulo the per-tile selection/swappable borders.
No new bitmap is needed.

Changes:

- **Suppress borders/highlights when solved.** In `PuzzleBoard`, when
  `board.isSolved` is true, draw no per-tile border (skip the `selected` /
  `canSwap` branches). The result is a seamless image.
- **Delay navigation.** The existing solve `LaunchedEffect` in `BoardScreen`
  currently calls `onSolved()` immediately on the unsolved→solved transition.
  Wrap that call in a `delay(REVEAL_HOLD_MS)` so the board holds the finished
  image for ~2s, then navigates. Cancellation on recomposition is automatic
  (LaunchedEffect); the guard (`wasUnsolved`, id/difficulty match) is unchanged.

The timer already stops on solve (`timerJob?.cancel()` in the ViewModel), so the
hold adds no time.

## Feature 2 — Hint

### UI (BoardScreen)

- A `Hint (n)` PillButton sits **below the board, in a Row beside the existing
  Pause button** — Hint on the left, Pause on the right, each `weight(1f)`
  within the same `widthIn(max = 560.dp)` container the board uses, so together
  they span the board width. Hint uses `filled = false` (secondary, like
  Pause). Label shows remaining count (`"Hint (${remaining})"`);
  `enabled = remaining > 0`. Disabled state uses the existing PillButton
  disabled styling. (Note: `PillButton` must support an `enabled` param; if it
  does not yet, add one defaulting to `true` — verify during implementation.)
- Tapping Hint: call `game.useHint()` (decrements the count) and start a
  UI-local overlay: the full image drawn over the board (`ContentScale.Crop`,
  `fillMaxSize`, matching the board's square) at full opacity for `HINT_MS`,
  fading in/out over `HINT_FADE_MS`, then gone. Implemented with an
  `Animatable` alpha + a coroutine that holds then fades — **UI-local state,
  not ViewModel state**.
- Re-tapping while a hint is showing is a no-op (ignore if overlay active),
  mirroring the `animating` guard already used for swipes.
- **Reduced motion:** honor `rememberReducedMotion()` — fades become instant
  snaps (snapTo 1f, hold, snapTo 0f); the 2.5s duration is unchanged (it is
  information, not decoration).

### Full-image source (ViewModel)

- New `ImageSlicer.loadFull(...)` overloads that reuse the **exact same
  center-crop + bounded decode** the slicer uses, returning a single
  `ImageBitmap` of the cropped square. Two overloads mirroring `slice`:
  - `loadFull(context, @DrawableRes imageRes): ImageBitmap?`
  - `loadFull(imagePath: String): ImageBitmap?`
  Both share a private `cropSquare(full: Bitmap): ImageBitmap` that applies the
  same `min(w,h)` center crop as `sliceBitmap`, so the hint image lines up
  pixel-for-pixel with the tiles. Return null on decode failure.
- `GameViewModel` exposes `val fullImage: StateFlow<ImageBitmap?>`. Loaded once
  per board in the same `loadTiles` coroutine (off the main thread, `default`
  dispatcher), cleared to null in `startBoard` / `exitBoard` alongside tiles.
  A null full image simply means the hint overlay shows nothing (button still
  decrements; acceptable and rare — same source that produced the tiles).

### Hint count (ViewModel + pure core)

- Pure decision core `HintState` (new file under `domain/model`), no Android
  types, so it can be property-tested:

  ```kotlin
  data class HintState(val remaining: Int) {
      val canUse: Boolean get() = remaining > 0
      fun use(): HintState = if (canUse) HintState(remaining - 1) else this
      companion object {
          const val MAX = 3
          fun fresh() = HintState(MAX)
      }
  }
  ```

- `GameViewModel`:
  - `private val _hints = MutableStateFlow(HintState.fresh())`
  - `val hintsRemaining: StateFlow<Int>` derived from `_hints` (map to
    `.remaining`), or expose `_hints` directly — implementer's call.
  - `fun useHint() { _hints.value = _hints.value.use() }`
  - Reset to `HintState.fresh()` in `startBoard` and `restart` (both begin a
    fresh game).

## Data flow

```
startBoard / restart
  ├─ _hints = HintState.fresh()          (3)
  ├─ _fullImage = null → load in loadTiles coroutine
  └─ fresh scramble as today

Hint tap  → game.useHint()  → _hints decrements → button label/enabled update
          → BoardScreen shows fullImage overlay 2.5s (UI-local), timer untouched

solve     → board.isSolved → borders suppressed (seamless image on board)
          → LaunchedEffect: delay(2000) → onSolved()  (timer already stopped)
```

## Files touched

| File | Change |
|------|--------|
| `domain/model/HintState.kt` | **new** — pure hint-count core |
| `data/ImageSlicer.kt` | add `loadFull` overloads + shared `cropSquare` |
| `game/GameViewModel.kt` | `_hints`, `hintsRemaining`, `useHint`, `_fullImage`/`fullImage`, load full in `loadTiles`, reset in `startBoard`/`restart`, clear in `exitBoard` |
| `ui/screens/BoardScreen.kt` | Hint button + overlay, suppress borders when solved, delay `onSolved()` by `REVEAL_HOLD_MS` |
| `test/.../HintStatePropertiesTest.kt` | **new** — PBT for hint core |

## Error handling

- Full image fails to decode → `fullImage` stays null → hint overlay renders
  nothing for its duration; button still decrements. No crash, no error screen
  (the tiles already loaded from the same source, so this is essentially
  unreachable in practice).
- Hint tapped at 0 → button is disabled, so unreachable; `useHint()` is also a
  no-op at 0 (`HintState.use` guards).

## Testing

Property-based (PBT extension is enabled):

- `HintState.use()` never produces `remaining < 0`.
- `use()` decrements by exactly 1 iff `remaining > 0`, else is identity.
- `use()` applied `MAX` times from `fresh()` reaches 0; further `use()` stays 0.
- `fresh().remaining == MAX`.

Reveal timing and overlay are UI glue (no new pure logic) — covered by
build/lint and on-device verification, consistent with existing UI features.

## Extension compliance

- **Property-Based Testing (Full):** satisfied — `HintState` core has PBTs.
  Reveal/overlay are UI glue with no pure decision logic (N/A for PBT).
- **Security Baseline:** N/A — no new inputs, permissions, IO, or data exposure;
  `loadFull` reuses the existing vetted decode path.
- **Resiliency Baseline:** null-full-image path handled gracefully (no crash);
  decode is bounded (`inSampleSize`) as the slicer already is.
