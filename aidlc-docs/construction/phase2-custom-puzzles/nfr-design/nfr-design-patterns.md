# NFR Design Patterns — Phase 2 Custom Photo Puzzles

On-device, single-user, offline. Distributed patterns are not used.

## Performance & Memory Patterns

### PP-1 Two-pass bounded decode (PM-1)
- Pass 1: `BitmapFactory.Options(inJustDecodeBounds=true)` reads dimensions
  without allocating pixels.
- Compute `inSampleSize` via pure `ImageMath.computeInSampleSize(...)` toward the
  ~1024 px target.
- Pass 2: decode with that sample size — the decoded bitmap is a fraction of the
  original, bounding peak memory and preventing OOM on multi-MP photos.

### PP-2 Dispatcher confinement (PM-2)
- The whole import (decode/EXIF/crop/scale/write/slice) runs in
  `withContext(io)`; CPU-heavy scaling may use `default`. UI thread never touches
  bitmaps or files.

### PP-3 Explicit bitmap lifecycle (PM-3)
- Intermediate bitmaps are used in a narrow scope and `recycle()`d (or dropped)
  once the next stage has its output; only the final saved files + the tiles for
  the active board are retained. No bitmap cache.

### PP-4 Ephemeral capture file (PM-4)
- Camera writes to app **cache**; the temp file is deleted after a successful
  import or on cancel — `try/finally` guarantees cleanup even on error.

### PP-5 Minimum-duration transition (PM-5)
- Generating shows for `max(importTime, ~500 ms)` using a timestamp gate so a
  fast import still reads as an intentional step without blocking.

## Resilience Patterns

### RP-1 Result-typed fail-safe import (R-1, S-15)
- `PhotoImporter.import` returns a sealed `ImportResult`
  (`Success | TooSmall | DecodeFailed | IoFailed`) instead of throwing. The
  ViewModel maps failures to a generic `CreateState.Error(message)` and returns
  to the chooser. No exception reaches the UI.

### RP-2 Lifecycle-bound camera (R-3)
- CameraX use cases bind to the screen's `LifecycleOwner`; leaving the Camera
  composable unbinds/releases the camera automatically (no leaked camera/session).

### RP-3 Missing-file degradation (R-2)
- Reuses Phase-1 `PuzzleFileStore.filesExist` + BR-8: a saved custom puzzle whose
  file vanished is shown unavailable / offered for removal, never crashes.

## Security Patterns

### SP-1 Single image-validation boundary (S-05, S-11)
- All source validation (resolvable URI, decodes, `min edge ≥ ~300 px`, size
  caps) lives in `PhotoImporter` — one choke point, not scattered in UI.

### SP-2 Safe logging (S-03)
- No photo bytes, URIs' contents, EXIF, or PII logged; only non-sensitive
  status. Debug logs stripped in release (Phase 5).

### SP-3 Least-privilege permission flow (permission NFR)
- CAMERA requested only on the Take-photo path; picker path requests nothing.
  Permanently-denied routes to an explanatory screen with Settings + picker
  fallback — no silent capability loss.

### SP-4 Generic user errors (S-09)
- All create-flow errors surface as friendly copy ("This photo is too small",
  "Couldn't read that photo", "Something went wrong") — no internals.

## Maintainability Pattern

### MP-1 Ports & adapters continued
- Pure `ImageMath`/`CustomPuzzleNamer` (domain, PBT-tested) separated from the
  Android adapters (`PhotoImporter`, `CameraController`). ViewModel depends on
  interfaces; Hilt binds implementations — testable with fakes.

## Not Used (justified)
- **Caching / queues / circuit breakers / retries**: single local operation, no
  remote calls or high throughput. N/A.
- **Background WorkManager job**: import is short and user-interactive
  (Generating screen); a foreground coroutine suffices. N/A this phase.
