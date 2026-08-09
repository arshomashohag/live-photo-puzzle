# Code Generation Plan — Phase 2: Custom Photo Puzzles

**Unit**: `phase2-custom-puzzles`
**Project type**: Brownfield (modify-in-place; no `X_new.kt` duplicates).
**Workspace root**: /Users/shohag/Desktop/Development/android/live-photo-puzzle
**Code location**: `app/src/main/java/com/tessera/puzzle/…`; tests in
`app/src/test/…` and `app/src/androidTest/…`. Docs → `aidlc-docs/construction/
phase2-custom-puzzles/code/`.

Single source of truth for generation. Bite-sized, verifiable steps. Tests
generated here; executed in Build and Test.

## Unit Context
- **Implements**: FR2-1..FR2-7, NFR2-1..NFR2-6.
- **Reuses**: PuzzleRepository (addCustomPuzzle/deletePuzzle), PuzzleFileStore,
  ImageSlicer, GameViewModel, adjacent-swap engine, Hilt/StateFlow.
- **New**: CameraX + exifinterface deps; pure ImageMath + namer; PhotoImporter;
  CameraController; CreateViewModel; create/library/permission screens; nav +
  Home CTA wiring; DI modules.

---

## Step 1: Dependencies + manifest
- [ ] `libs.versions.toml`: add CameraX 1.3.4 (`camera-core`, `camera-camera2`,
  `camera-lifecycle`, `camera-view`) and `androidx-exifinterface` 1.3.7.
- [ ] `app/build.gradle.kts`: add those `implementation`s.
- [ ] `AndroidManifest.xml`: add `<uses-permission android:name=
  "android.permission.CAMERA"/>` and `<uses-feature
  android:name="android.hardware.camera.any" android:required="false"/>`.
  (No INTERNET.)

## Step 2: Pure image math + namer (domain)
- [ ] Create `domain/model/ImageMath.kt` (`computeInSampleSize`,
  `centerCropSquare` → `CropRect`, `isLargeEnough`) and `CropRect`.
- [ ] Create `domain/model/CustomPuzzleNamer.kt` (`nextName`).

## Step 3: Image-math PBT (Kotest)
- [ ] Create `test/.../domain/ImageMathPropertiesTest.kt`: sample-size keeps
  target & power-of-two & monotonic; center-crop within bounds & square-identity;
  isLargeEnough boundary; naming format. Seeded.

## Step 4: PhotoImporter (image layer)
- [ ] Create `image/ImportResult.kt` (sealed: Success/TooSmall/DecodeFailed/IoFailed).
- [ ] Create `image/PhotoImporter.kt` interface + `PhotoImporterImpl`:
  two-pass bounded decode via ContentResolver; EXIF upright; center-crop; scale
  to ~1024; write `<id>.jpg` + `<id>_thumb.jpg` (256px) through PuzzleFileStore
  paths; slice via ImageSlicer; return ImportResult. Off-main (IO). Bitmaps
  released; no PII logging.

## Step 5: CameraController (camera layer)
- [ ] Create `camera/CameraController.kt` interface + `CameraControllerImpl`:
  CameraX Preview + ImageCapture; `bind(lifecycleOwner, previewView)`;
  `suspend capture(): Uri` to app cache; `hasCamera(context)`.

## Step 6: DI modules
- [ ] `di/ImageModule.kt` (binds PhotoImporter), `di/CameraModule.kt`
  (provides CameraController).

## Step 7: CreateViewModel (presentation)
- [ ] Create `presentation/CreateState.kt` (sealed states) and
  `presentation/CreateViewModel.kt` (@HiltViewModel): StateFlow<CreateState>;
  chooseCamera/choosePicker; onPermissionResult; onCaptured/onPicked → Review;
  retake/accept → PickSize; pickSize → Generating → import (min 500ms) →
  addCustomPuzzle → startBoard handoff → Ready; map ImportResult failures →
  Error. Delete temp capture file after import/cancel.

## Step 8: Create-flow screens
- [ ] `ui/screens/create/CreateChooserSheet.kt` (Take photo / Choose photo)
- [ ] `ui/screens/create/CameraScreen.kt` (CameraX PreviewView via AndroidView +
  shutter; content descriptions)
- [ ] `ui/screens/create/ReviewScreen.kt` (auto-cropped preview; Retake/Accept)
- [ ] `ui/screens/create/PickSizeScreen.kt` (Easy/Medium/Hard cards)
- [ ] `ui/screens/create/ImportGeneratingScreen.kt` (blueprint animation)
- [ ] `ui/screens/create/PermissionNeededScreen.kt` (rationale + Open Settings +
  Choose from photos)

## Step 9: Library screen + delete
- [ ] `ui/screens/MyPuzzlesScreen.kt`: grid of CUSTOM puzzles (newest first,
  thumbnail + name), tap → pick size → play; delete → confirm dialog →
  deletePuzzle; empty state.
- [ ] Add `MyPuzzles`/delete affordances; reuse RegistrationFrame/theme.

## Step 10: Navigation + Home wiring
- [ ] `TesseraApp.kt`: add routes (create graph, myPuzzles); Home CTA opens the
  chooser instead of "Coming soon"; wire Ready → Board; Home "My puzzles" row →
  MyPuzzles.
- [ ] `HomeScreen.kt`: CTA → open create chooser; make "My puzzles" row navigate.
- [ ] `PuzzleSelectScreen.kt`: already lists all puzzles from repo — confirm
  custom ones appear; add custom section if needed.

## Step 11: Photo-picker integration
- [ ] Wire `PickVisualMedia` launcher in the create flow (no storage permission);
  route result → onPicked.

## Step 12: Instrumented tests
- [ ] `androidTest/.../CustomPuzzleFlowTest.kt`: given a bundled test image URI,
  import → addCustomPuzzle → appears in library (source=CUSTOM) → delete → row +
  files removed. Permission-denied UI state (state-level).

## Step 13: Docs summary
- [ ] `aidlc-docs/construction/phase2-custom-puzzles/code/implementation-summary.md`.

## Traceability
FR2-1/2 → Steps 5,7,8,11 · FR2-3 → Steps 2,4 · FR2-4/5 → Steps 9,7 · FR2-6 →
Steps 5,8 · FR2-7 → Step 2 · NFR2-1 → Step 4 · NFR2-2 → Steps 4,5,7 · PBT →
Step 3.

## Scope / Estimated
- 13 steps; ~18 files created + ~3 modified. Tests: ImageMath PBT + instrumented
  custom-puzzle flow. Live camera capture = manual device test.
