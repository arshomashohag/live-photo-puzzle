# Performance Test Instructions

Phase 1 has no load/throughput surface (offline, single-user, on-device). The
relevant performance concerns are UI responsiveness and I/O not blocking the
main thread (NFR P-1..P-4).

## Performance Expectations
- **No main-thread I/O**: DB/file work runs on `Dispatchers.IO`; tile slicing on
  `Dispatchers.Default`.
- **Autosave**: debounced (~750 ms, coalescing) — no write-per-tap.
- **Frame smoothness**: board interactions stay at 60 fps on mid-range devices.
- **Startup**: no measurable regression vs. the Phase-0 slice.

## How to Verify (manual, on device)
### 1. StrictMode / main-thread checks
Enable StrictMode in a debug run (optional) and confirm no disk-read/write
violations on the main thread during play, save, and completion.

### 2. Jank inspection
- Use Android Studio **Profiler** (or `adb shell dumpsys gfxinfo com.tessera.puzzle`)
  while rapidly swapping tiles on a 5×5 board.
- **Expected**: no sustained dropped frames; autosave bursts coalesce.

### 3. Autosave write volume
- Rapidly tap many swaps, then pause.
- **Expected**: a single debounced write after the burst (plus the forced save on
  Pause), not one write per tap. Verify via DB-access logging in a debug build
  (no PII logged).

## Optimization
If jank appears: confirm tile bitmaps are sliced off-thread and cached per
(puzzle, difficulty), and that recomposition is scoped (StateFlow, not whole-tree
recomposition).
