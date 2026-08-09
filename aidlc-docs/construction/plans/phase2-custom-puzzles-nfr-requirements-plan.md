# NFR Requirements Plan — Phase 2: Custom Photo Puzzles

**Unit**: `phase2-custom-puzzles`

## Plan Checklist
- [x] Capture tech-stack decisions (CameraX modules + versions)
- [x] Performance/memory NFRs (OOM safety, off-main-thread, bitmap release)
- [x] Security NFRs (image input validation, no PII/EXIF logging, permission)
- [x] Resiliency NFRs (capture/decode failure, missing file)
- [x] Accessibility NFRs (camera/review/library controls)
- [x] Testability NFRs (image-math PBT, instrumented create/delete)
- [x] Collect answers; resolve ambiguities
- [x] Generate nfr-requirements.md + tech-stack-decisions.md
- [x] Compliance summary

---

## Clarifying Questions

Answer each after the `[Answer]:` tag. Most NFRs are fixed by requirements; these
are the open knobs.

## Question 1: CameraX capture approach
For taking the photo, which CameraX use case?

A) ImageCapture (still photo) only — capture a single frame to a file; simplest and exactly what a puzzle needs — recommended

B) ImageCapture + a live Preview with a capture button (standard camera UI) — this is implied either way; the question is whether to also add tap-to-focus/flash controls now

X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 2: EXIF orientation handling
Phone photos carry EXIF rotation. How to handle it during import?

A) Read EXIF and rotate the bitmap upright before crop/slice (so tiles aren't sideways) — recommended

B) Ignore EXIF (may produce rotated puzzles on some devices)

X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 3: Temporary capture file cleanup
Camera capture writes a temp file before import. After import (or cancel):

A) Delete the temp capture file once the processed image is saved (or on cancel) — keep only the final files — recommended

B) Leave temp files in cache (Android clears cache eventually)

X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 4: Instrumented test depth for camera
Camera hardware is hard to test in CI. What instrumented coverage?

A) Test the parts that don't need real camera hardware: photo-picker import → save → library → delete → files removed; permission-denied UI state. Leave live camera capture to manual device testing — recommended

B) Attempt full camera automation (fragile, needs a camera-equipped emulator/device in CI)

X) Other (please describe after [Answer]: tag below)

[Answer]: A
