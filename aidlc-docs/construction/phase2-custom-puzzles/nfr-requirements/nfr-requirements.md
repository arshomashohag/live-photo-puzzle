# NFR Requirements — Phase 2 Custom Photo Puzzles

Cloud/web NFRs remain N/A (offline on-device app).

## Performance & Memory (NFR2-1)
- **PM-1**: Never decode a photo at full resolution — always `inSampleSize`
  downsample toward ~1024 px first (guards OOM on high-res photos).
- **PM-2**: All decode/EXIF/crop/scale/slice/file I/O off the main thread
  (coroutines + IO/Default dispatchers).
- **PM-3**: Bitmaps are released/recycled promptly; only the final files + tiles
  are retained. No unbounded caching.
- **PM-4**: Temp capture file deleted after import or cancel (Q3=A).
- **PM-5**: Generating screen minimum ~500 ms; import must not block UI.

## Security (enforced applicable rules)
- **S-05**: Validate the source (URI resolvable, decodes, `min(w,h) ≥ ~300 px`);
  reject oversized/malformed with a friendly message.
- **S-03**: No photo bytes, URI contents, EXIF, or PII logged.
- **S-09**: Generic user-facing errors; no stack traces / internal paths.
- **S-15**: Every camera/decode/IO call has explicit error handling; fail-safe
  (return to a safe screen); bitmaps/streams released on error paths.
- **S-11**: Image validation/processing isolated in `image/` (PhotoImporter +
  pure ImageMath), camera in `camera/` — not scattered into UI.
- **S-10**: CameraX + exifinterface pinned in the version catalog.
- **Permission**: CAMERA requested only at capture; least privilege; no storage
  permission (picker used). N/A: cloud/web rules (encryption-in-transit, IAM,
  network, HTTP headers, auth, etc.).

## Privacy (NFR2-3)
- User photos stay in app-internal storage; no upload/analytics/network.

## Resiliency (NFR2-4)
- **R-1**: Capture failure / decode failure / IO failure → friendly error,
  return to chooser, never crash.
- **R-2**: Missing saved custom image on load → discard/unavailable + notice
  (reuses Phase-1 BR-8 + `PuzzleFileStore.filesExist`).
- **R-3**: Camera lifecycle bound to the composable/owner; released on leave.

## Accessibility (NFR2-5)
- Camera shutter, retake/accept, size options, library items, and delete have
  content descriptions and ≥48 dp targets. Permission screens actionable via
  TalkBack.

## Testability (NFR2-6)
- **T-1 (PBT)**: pure `ImageMath` (sample-size, center-crop, isLargeEnough) and
  `CustomPuzzleNamer` covered by Kotest property tests (FD PBT-01 table).
- **T-2 (instrumented)**: photo-picker import → save → appears in library →
  delete → files removed; permission-denied UI state (Q4=A). Live camera capture
  is manual device testing.
- **T-3 (manual device matrix)**: camera capture, EXIF-rotated photos, denial
  paths, no-camera fallback, restart survival of a saved custom puzzle.

## Verification Gates (Build and Test)
- Build succeeds; image-math PBT + naming pass; instrumented picker/create/delete
  pass on device; lint clean.
- No OOM on a large (e.g. 4000×3000) photo — downsample verified.
- No PII/photo logging; temp files cleaned; permission requested only at capture.
