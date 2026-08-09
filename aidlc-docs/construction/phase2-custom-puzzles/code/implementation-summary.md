# Implementation Summary — Phase 2: Custom Photo Puzzles

Brownfield changes (modify-in-place). App code under `app/src/…`.

## Created — application code
- `domain/model/ImageMath.kt` (computeInSampleSize, centerCropSquare, isLargeEnough) + `CropRect`
- `domain/model/CustomPuzzleNamer.kt`
- `image/ImportResult.kt` (sealed) + `image/PhotoImporter.kt` (interface + impl: two-pass bounded decode, EXIF upright, center-crop, scale, write image+thumb, memory-safe/fail-safe)
- `camera/CameraController.kt` (interface + CameraX impl: Preview + ImageCapture, hasCamera, capture-to-cache)
- `di/ImageModule.kt`, `di/CameraModule.kt`
- `presentation/CreateState.kt`, `presentation/CreateViewModel.kt` (StateFlow flow machine, min-duration Generating, temp-file cleanup, naming)
- `ui/screens/create/`: CreateChooserScreen, CameraScreen (AndroidView PreviewView), ReviewScreen, CreateFlowScreens (PickSize, ImportGenerating, PermissionNeeded, CreateError), CreateFlowHost (permission + PickVisualMedia launchers, state switch)
- `ui/screens/MyPuzzlesScreen.kt` (library grid newest-first, thumbnail, delete-confirm dialog, empty state)

## Created — tests
- `test/domain/ImageMathPropertiesTest.kt` (Kotest PBT: sample-size, crop, naming)
- `androidTest/image/CustomPuzzleFlowTest.kt` (import→files written→delete cleanup; too-small rejection)

## Modified
- `gradle/libs.versions.toml`, `app/build.gradle.kts` (CameraX 1.3.4, exifinterface 1.3.7)
- `AndroidManifest.xml` (CAMERA permission + optional camera feature; still no INTERNET)
- `game/GameViewModel.kt` (+ `deleteCustomPuzzle`)
- `TesseraApp.kt` (routes CREATE, MY_PUZZLES; Home CTA → create; My-puzzles nav; Ready → startBoard → Board)
- `ui/screens/HomeScreen.kt` (CTA functional; My-puzzles row; removed "Coming soon")

## Requirement / BR / NFR mapping
- FR2-1/2 (camera+picker create) → CameraController, CreateFlowHost, PickVisualMedia.
- FR2-3 (processing) → PhotoImporter + ImageMath (downsample/crop/thumbnail/slice).
- FR2-4/5 (library/delete) → MyPuzzlesScreen + GameViewModel.deleteCustomPuzzle (reuses repo cleanup).
- FR2-6 (permission) → permission launcher + PermissionNeededScreen (Settings + picker fallback).
- FR2-7 (naming) → CustomPuzzleNamer.
- NFR2-1 (memory) → two-pass bounded decode, off-main, bitmap release, temp cleanup.
- NFR2-2 (security) → validation floor + result-typed failures + no PII logging + generic errors.
- PBT → ImageMathPropertiesTest.

## Verification (Build and Test stage)
- `:app:assembleDebug` + `:app:testDebugUnitTest`; instrumented `CustomPuzzleFlowTest`
  on device. Live camera capture = manual device test.
