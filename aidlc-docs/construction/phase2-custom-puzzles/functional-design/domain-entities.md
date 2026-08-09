# Domain Entities — Phase 2 Custom Photo Puzzles

Reuses Phase-1 persistence types. New value types are technology-agnostic
(no Android imports in domain).

## Reused (Phase 1) — unchanged
- `PuzzleRecord` (source=CUSTOM for created puzzles; `ImageRef.FileRef`).
- `ImageRef.FileRef(imagePath, thumbPath)` — the full processed image + thumbnail.
- `PuzzleRepository.addCustomPuzzle/deletePuzzle`, `PuzzleFileStore`.

## New value: PhotoSource
The origin the user picked from.
```
enum PhotoSource { CAMERA, PICKER }
```

## New value: ImportSpec
Parameters for turning a source image into a puzzle (pure inputs).
| Field | Type | Notes |
|---|---|---|
| targetEdgePx | Int | Full processed image edge (~1024, Q6). |
| thumbEdgePx | Int | Thumbnail edge (256, Q1). |
| minSourceEdgePx | Int | Reject smaller than this (~300, Q3). |

## New value: SampleSizeResult (pure, PBT-able)
Output of the downsample calculation.
| Field | Type | Notes |
|---|---|---|
| inSampleSize | Int | Power-of-two BitmapFactory sample size. |

## New value: CropRect (pure, PBT-able)
Center-crop square region within a source of width×height.
| Field | Type |
|---|---|
| left, top, size | Int |

## New value: ImportedPuzzle
Result of a successful import (files already written).
| Field | Type | Notes |
|---|---|---|
| record | PuzzleRecord | source=CUSTOM, FileRef(imagePath, thumbPath), auto-name |
| difficulty | Difficulty | chosen size at creation (Q3=A) |

## New state: CreateState (presentation)
The create-flow state machine value (in CreateViewModel).
```
sealed interface CreateState {
  data object Chooser                 // Take photo / Choose photo
  data object RequestingPermission
  data object PermissionDenied        // permanently denied → settings + picker
  data object Camera                  // CameraX preview
  data class  Review(sourceUri)       // auto-cropped preview; retake/accept
  data class  PickSize(sourceUri)
  data class  Generating(sourceUri, difficulty)
  data class  Error(message)          // friendly, generic (SECURITY-09)
  data class  Ready(puzzleId, difficulty)  // → navigate to Board
}
```

## Naming
`CustomPuzzleNamer.nextName(existingCustomCount): String` → "My Puzzle N",
N = existingCustomCount + 1 (Q5 ordering is by createdAt desc; name is stable).
