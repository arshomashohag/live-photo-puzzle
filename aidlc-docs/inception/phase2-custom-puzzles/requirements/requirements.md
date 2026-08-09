# Requirements — Phase 2: Custom Photo Puzzles

## Intent Analysis
- **User request**: Add the differentiating feature — create puzzles from the
  user's own photos (camera or photo picker), play, save, and delete them.
- **Request type**: New Feature (major subsystem) on the Phase-1 architecture.
- **Scope**: Multiple components (camera, image processing, create/library UI,
  repository wiring already partly present).
- **Complexity**: Complex (device permissions, bitmap memory safety, new flows).
- **Depth**: Comprehensive.

## Locked Decisions (carried + clarified)
| Decision | Value |
|---|---|
| Capture | **CameraX** |
| Existing-photo | **PickVisualMedia** system photo picker (no storage permission) |
| Permission | **CAMERA** only, requested at capture time |
| Image storage | processed image + thumbnail in `filesDir/puzzles/<id>.jpg` / `<id>_thumb.jpg`; Room row with `ImageRef.FileRef` (schema already present) |
| Privacy | photos never leave device; offline; no network |
| Gameplay | adjacent-only swap engine reused unchanged |
| Architecture | Hilt + StateFlow + repositories reused |

## Clarifying Answers (all recommended, "A")
- **Q1=A** Entry points: **both** camera capture and "choose from photos"
  (CTA opens a Take photo / Choose photo chooser).
- **Q2=A** Framing: **auto center-crop** to square (Review screen shows the
  square result to accept/retake). No manual crop UI.
- **Q3=A** Difficulty: **pick size once at creation** (Easy/Medium/Hard) →
  generate → play; the saved puzzle is **replayable at any size** later from
  Puzzle Select.
- **Q4=A** Placement: custom puzzles appear in a dedicated **"My puzzles"**
  library (play/delete) **and** in Puzzle Select alongside bundled puzzles.
- **Q5=A** Naming: **auto-name** ("My Puzzle 1", 2, …); no typing.
- **Q6=A** Processing: downsample to **~1024 px** square source (matches
  bundled), then slice.
- **Q7=A** Delete: **confirmation dialog/sheet** before delete.
- **Q8=A** Permission denial: friendly **"Camera access needed"** screen with an
  Open-Settings button and a **"Choose from photos"** alternative.

## Functional Requirements

### FR2-1 Create flow (camera)
Home CTA → chooser → **Camera** (CameraX preview) → Capture → **Review**
(auto-cropped square; Retake / Accept) → **Pick size** → **Generating** →
**Board**. On accept+generate, the processed image+thumbnail are written to
`filesDir/puzzles/` and a custom `PuzzleRecord` (source=CUSTOM, FileRef,
auto-name) is saved to Room; a board is started at the chosen difficulty.

### FR2-2 Create flow (photo picker)
Home CTA → chooser → **Choose photo** (`PickVisualMedia`) → **Review** → same
pick-size → generate → save → play path. No storage permission requested.

### FR2-3 Image processing
Decode the source URI with bounded memory (`inSampleSize` downsample to ~1024 px
target), **center-crop to square**, produce the full puzzle image + a small
thumbnail, and slice into N×N tiles (reuse `ImageSlicer`). Handle
invalid/corrupt/huge images gracefully (no OOM, no crash); release bitmaps.

### FR2-4 Library ("My puzzles")
A grid of saved custom puzzles (thumbnail + name + created date). Tap → choose
size → play. Long-press or a menu → **Delete** (with confirmation). Empty state
when none exist. Custom puzzles also appear in the relevant Puzzle Select lists.

### FR2-5 Delete + cleanup
Delete removes the Room row, its SavedBoard/BestScore rows, and its image +
thumbnail **files** (reusing Phase-1 `PuzzleRepository.deletePuzzle` +
`PuzzleFileStore`). Confirmation required (Q7=A).

### FR2-6 Permissions & denial
Request CAMERA only when the user chooses camera capture. Handle: granted →
camera; denied (first) → rationale then re-request; permanently denied → the
"Camera access needed" screen with Open-Settings + "Choose from photos"
fallback; no camera hardware → fall back to picker.

### FR2-7 Naming
Auto-generate a stable unique name ("My Puzzle N", N = count+1) at save.

## Non-Functional Requirements

### NFR2-1 Performance / memory (SECURITY-05, NFR-2)
- Downsample before decode; never load full-resolution bitmaps.
- All decode/crop/slice/file I/O off the main thread (coroutines + IO/Default).
- Bounded, released bitmaps; no unbounded caching; guard against OOM.

### NFR2-2 Security (enforced applicable rules)
- **S-05**: validate image input (content resolvable, decodes, within size
  caps); reject/handle malformed. **S-15**: fail-safe on decode/IO errors
  (user-friendly message, resources released, DB left consistent). **S-03**: no
  photo bytes/paths-contents/PII logged. **S-09**: no stack traces to users.
  **S-10**: any new deps (CameraX) pinned in the catalog. Cloud/web rules N/A.

### NFR2-3 Privacy
- User photos stay in app-internal storage; no upload, analytics, or network.
  Only CAMERA permission; still no `INTERNET`.

### NFR2-4 Resiliency
- Corrupt/failed capture or decode → graceful error, return to a safe screen.
- Missing image file for a saved custom puzzle → discard + friendly notice
  (reuses Phase-1 BR-8 path; `PuzzleFileStore.filesExist`).

### NFR2-5 Accessibility
- Camera/review/library controls have content descriptions and ≥48dp targets;
  permission screens are readable and actionable via TalkBack.

### NFR2-6 Testing (with PBT)
- **Unit/PBT**: image-processing math (sample-size computation for a target
  edge; center-crop rectangle bounds) as pure functions; custom-puzzle naming;
  reuse Phase-1 mapping/engine PBT.
- **Instrumented**: create→save→appears-in-library→delete→files-removed; permission
  denial UI where practical.
- **Manual device**: camera capture, picker, denial paths, restart survival.

## Out of Scope (this phase)
- Manual crop/rotate UI (auto-crop only), filters/effects, cloud backup,
  sharing, multi-photo albums. Dark theme / tablet adaptivity remain Phase 3.

## Key Requirements Summary
Phase 2 delivers on-device **custom photo puzzles**: capture (CameraX) or pick
(PickVisualMedia) → auto-crop to square → downsample (~1024 px, OOM-safe) →
generate → **pick size** → play, with puzzles **saved** to a "My puzzles" library
(auto-named), **replayable** at any difficulty, and **deletable** with
confirmation and full file cleanup. CAMERA-only, offline, photos never leave the
device. Reuses the Phase-1 architecture, adjacent-swap engine, and
persistence/file layers; adds camera, image-processing, and create/library UI.
