# Test Instructions — Phase 2 (Custom Photo Puzzles)

## Build
```bash
make apk            # or ./gradlew :app:assembleDebug
```

## Unit + property-based tests (JVM)
```bash
./gradlew :app:testDebugUnitTest
```
Expected: 29 pass, 0 failures. New: `ImageMathPropertiesTest` (7 Kotest PBT).

## Static analysis
```bash
./gradlew :app:lintDebug
```
Expected: 0 errors. (Warnings present: mostly `GradleDependency` "newer version
available" — deferred to Phase 6 dependency review.)

## Instrumented (device/emulator required)
```bash
./gradlew :app:connectedDebugAndroidTest
```
- `image/CustomPuzzleFlowTest`: generated file URI → import → image+thumb files
  written → `filesExist` → delete cleanup; too-small (200px) rejected.
- `data/PuzzlePersistenceTest` (Phase 1): Room save/load, best-score, cascade.

## Security checks (Phase 2)
- **S-05** input validation: `CustomPuzzleFlowTest.import_rejectsTooSmallImage`;
  decode-failure returns DecodeFailed (no crash).
- **S-15** fail-safe/cleanup: PhotoImporter catches OOM/IO, releases bitmaps;
  temp capture file deleted after import/cancel.
- **S-03** no PII logging: `grep -rn "Log\\." app/src/main/java/com/tessera/puzzle/image
  app/src/main/java/com/tessera/puzzle/camera` — only generic messages.
- **Permission least-privilege**: CAMERA requested only on Take-photo path;
  picker path requests nothing; `grep INTERNET AndroidManifest.xml` → none.

## Manual device matrix (pending a device)
Camera capture → review → size → generate → play; picker path; permission
grant/deny/permanently-deny (Settings + picker fallback); no-camera fallback;
EXIF-rotated photo upright; save/appears-in-library/delete+cleanup; restart
survival; large photo (4000×3000) no OOM.
