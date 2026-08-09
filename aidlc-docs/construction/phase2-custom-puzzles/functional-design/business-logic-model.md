# Business Logic Model — Phase 2 Custom Photo Puzzles

## Pure image math (domain — Android-free, PBT-able)

```
object ImageMath {
    // BR2-2: largest power-of-two so decoded edge stays >= targetEdgePx.
    fun computeInSampleSize(srcW: Int, srcH: Int, targetEdgePx: Int): Int

    // BR2-3: center square crop rectangle within srcW x srcH.
    fun centerCropSquare(srcW: Int, srcH: Int): CropRect   // (left, top, size)

    fun isLargeEnough(srcW: Int, srcH: Int, minEdgePx: Int): Boolean
}

object CustomPuzzleNamer {
    fun nextName(existingCustomCount: Int): String  // "My Puzzle N"
}
```

These are the PBT targets; they contain no Android types so they run on the JVM.

## Android-facing components (data/camera/image layers)

```
// image/ — orchestrates decode/crop/scale/write using ImageMath + Android Bitmap
interface PhotoImporter {
    // Returns Result: success(ImportedPuzzle) or failure(friendly reason).
    suspend fun import(source: Uri, difficulty: Difficulty): ImportResult
}

// camera/ — CameraX preview + capture to a temp file/uri
interface CameraController {
    fun bind(lifecycleOwner, previewView)     // bind preview+capture use cases
    suspend fun capture(): Uri                // capture to app cache; throws→handled
    fun hasCamera(context): Boolean
}
```

`ImportResult = Success(ImportedPuzzle) | TooSmall | DecodeFailed | IoFailed`
(mapped to a generic user message; no internals surfaced).

## Presentation (CreateViewModel — StateFlow, UDF)

```
@HiltViewModel CreateViewModel:
  val state: StateFlow<CreateState>
  fun chooseCamera(); fun choosePicker()
  fun onPermissionResult(granted, permanentlyDenied)
  fun onCaptured(uri); fun onPicked(uri)      // → Review(uri)
  fun retake(); fun accept()                   // Review → PickSize
  fun pickSize(difficulty)                     // → Generating; run import
  // On import success: addCustomPuzzle + start board → Ready(puzzleId, difficulty)
```

Reuses `PuzzleRepository`, `PuzzleFileStore`, `ImageSlicer`, `GameViewModel`.

## Key Data Flows

### DF2-1 Create via camera
Home CTA → Chooser → chooseCamera → permission (BR2-6) → Camera → capture(uri) →
Review(uri) → accept → PickSize → pickSize(d) → Generating → PhotoImporter.import
→ addCustomPuzzle → GameViewModel.startBoard → Ready → Board.

### DF2-2 Create via picker
Chooser → choosePicker → PickVisualMedia(uri) → Review → … (same tail as DF2-1).

### DF2-3 Import (memory-safe)
`import(uri, d)`: read bounds → `isLargeEnough` (BR2-4 reject) →
`computeInSampleSize` decode → `centerCropSquare` → scale to target → write
image+thumb (BR2-4) → slice for d. Off-main-thread; bitmaps released.

### DF2-4 Library + delete
My puzzles ← `observePuzzles()` filtered to source=CUSTOM, ordered newest-first →
tap: pick size → play; delete: confirm dialog → `deletePuzzle` (row + files).

### DF2-5 Replay saved custom at any size
Puzzle Select / My puzzles → choose difficulty → GameViewModel re-slices the
saved full image for that grid size (BR2-10).

---

## Testable Properties (PBT-01) — MANDATORY

Framework: Kotest (seeded, shrinking — PBT-07/08). Complements example tests
(PBT-10). All targets are the **pure** `ImageMath` / `CustomPuzzleNamer`.

| Property | Category | Statement |
|---|---|---|
| Sample-size keeps target resolution | Invariant | For all srcW,srcH ≥ target and target ≥ 1: with `s = computeInSampleSize(...)`, `srcW/s ≥ target` and `srcH/s ≥ target`; and `s` is the largest power of two with that property (halving `s`… i.e. `2s` would violate it when src is large enough). |
| Sample-size ≥ 1, power of two | Invariant | `computeInSampleSize(...) ≥ 1` and is a power of two for all valid inputs. |
| Sample-size monotonic in target | Invariant | Larger targetEdgePx never yields a larger sample size. |
| Center-crop within bounds | Invariant | `centerCropSquare(w,h)` yields `left≥0, top≥0, size=min(w,h), left+size≤w, top+size≤h`. |
| Center-crop square identity | Invariant | For square input (w==h): left=top=0, size=w. |
| Naming uniqueness/format | Invariant | `nextName(n)` == "My Puzzle ${n+1}"; distinct for distinct n. |
| isLargeEnough boundary | Invariant | `isLargeEnough(w,h,min)` == (min(w,h) ≥ min). |

### Components with no PBT properties
- `CameraController`, `PhotoImporter` I/O orchestration, ViewModel wiring, and UI
  are **No PBT properties identified** (Android/side-effectful; covered by
  instrumented + example tests).
