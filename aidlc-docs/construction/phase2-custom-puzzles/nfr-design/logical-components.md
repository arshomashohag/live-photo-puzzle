# Logical Components — Phase 2 Custom Photo Puzzles

Additive to Phase-1 layers; no external infrastructure.

## Component map

```
presentation/
  CreateViewModel (@HiltViewModel)
    ├─ StateFlow<CreateState>  (chooser/permission/camera/review/size/generating/error/ready)
    ├─ injects PhotoImporter, PuzzleRepository, GameViewModel handoff
    └─ maps ImportResult → CreateState

domain/  (Android-free, PBT-tested)
  model/ImageMath        (computeInSampleSize, centerCropSquare, isLargeEnough)
  model/CustomPuzzleNamer(nextName)
  model/ (CropRect, ImportSpec, ImportedPuzzle, PhotoSource, CreateState)

image/
  PhotoImporter (impl)   uses ImageMath + BitmapFactory + ExifInterface
    ├─ two-pass bounded decode, EXIF upright, center-crop, scale to ~1024
    ├─ write full image + 256px thumbnail via PuzzleFileStore
    └─ slice via ImageSlicer; returns ImportResult

camera/
  CameraController (impl) CameraX Preview + ImageCapture; capture()→cache Uri;
                          hasCamera(); lifecycle-bound

data/  (reused from Phase 1)
  PuzzleRepository.addCustomPuzzle / deletePuzzle
  PuzzleFileStore (filesDir/puzzles read/write/delete + filesExist)
  ImageSlicer

ui/screens/  (new + modified)
  CreateChooser (sheet)         Take photo / Choose photo
  CameraScreen                  CameraX PreviewView + shutter
  ReviewScreen                  auto-cropped preview; Retake/Accept
  PickSizeScreen                Easy/Medium/Hard
  ImportGeneratingScreen        blueprint animation, min 500ms
  MyPuzzlesScreen               grid (newest first), play, delete-confirm
  PermissionNeededScreen        rationale + Open Settings + Choose from photos
  HomeScreen (modified)         CTA opens CreateChooser (was "Coming soon")
  PuzzleSelectScreen (modified) show custom puzzles too

di/  (additions)
  ImageModule    binds PhotoImporter impl
  CameraModule   provides CameraController
```

## Component responsibilities

| Component | Responsibility | NFR ties |
|---|---|---|
| `ImageMath` | pure sample-size/crop/threshold math | PP-1, PBT |
| `PhotoImporter` | bounded decode→EXIF→crop→scale→write→slice; validation; ImportResult | PP-1..PP-3, RP-1, SP-1 |
| `CameraController` | CameraX capture, hasCamera, lifecycle release | RP-2, PM-4 |
| `PuzzleFileStore` (reused) | file write/delete/exists | RP-3, PP-4 |
| `CreateViewModel` | create-flow state machine (StateFlow), error mapping | RP-1, SP-4, MP-1 |
| create/library/permission screens | UI; content descriptions; ≥48dp | accessibility |
| Hilt modules | bind image/camera adapters | MP-1 |

## Integration pattern
- UDF: `CreateViewModel.state` (StateFlow) drives the create screens; user events
  → ViewModel → PhotoImporter/repository → new state. On success, hand off to the
  existing `GameViewModel.startBoard`.
- No servers, queues, caches, or network — consistent with offline single-user.
