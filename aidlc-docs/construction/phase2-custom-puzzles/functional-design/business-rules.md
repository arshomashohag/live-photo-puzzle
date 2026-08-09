# Business Rules — Phase 2 Custom Photo Puzzles

## BR2-1 Entry chooser (Q1=A entry points)
- Home "Create from camera" CTA opens a chooser: **Take photo** / **Choose photo**.
- Take photo → permission flow (BR2-6) → Camera. Choose photo → PickVisualMedia.

## BR2-2 Sample-size calculation (pure; PBT)
- `computeInSampleSize(srcW, srcH, targetEdgePx)`: the largest power of two such
  that both `srcW/inSampleSize >= targetEdgePx` and `srcH/inSampleSize >=
  targetEdgePx` (standard BitmapFactory downsample). Minimum 1. Guarantees the
  decoded bitmap is ≥ target on the short edge without loading full resolution
  (OOM-safe).

## BR2-3 Center-crop to square (pure; PBT) (Q2=A auto-crop)
- `centerCropSquare(w, h)`: `size = min(w, h)`, `left = (w-size)/2`,
  `top = (h-size)/2`. Result is fully within bounds; for a square input it is the
  whole image.

## BR2-4 Import pipeline (Q6=A ~1024px, Q1 thumb 256px)
1. Resolve source URI; read bounds (`inJustDecodeBounds`).
2. **Reject** if `min(srcW, srcH) < minSourceEdgePx (~300)` → friendly error
   (Q3=A). Reject if decode fails/corrupt → friendly error (never crash).
3. Decode with `computeInSampleSize(...)` toward ~1024px; center-crop to square;
   scale to exactly `targetEdgePx` if needed.
4. Write full image → `filesDir/puzzles/<id>.jpg`; write 256px thumbnail →
   `<id>_thumb.jpg`.
5. Slice into N×N tiles for the chosen difficulty (reuse ImageSlicer on the saved
   file).
- All steps off the main thread; bitmaps recycled/released after use (SECURITY-15).

## BR2-5 Save custom puzzle (Q5 ordering, naming)
- On successful import: create `PuzzleRecord(id=UUID, name=nextName(customCount),
  source=CUSTOM, imageRef=FileRef(imagePath, thumbPath), createdAt=now,
  deletable=true)` via `PuzzleRepository.addCustomPuzzle`.
- Start a board at the chosen difficulty; navigate to Board.
- Library orders custom puzzles by `createdAt` **descending** (newest first).

## BR2-6 Camera permission (Q8=A)
- Request CAMERA only when the user chooses **Take photo**.
- Granted → Camera. Denied (revocable) → show rationale, allow re-request.
- Permanently denied → **"Camera access needed"** screen with **Open Settings**
  and **Choose from photos** alternative.
- No camera hardware (`PackageManager.FEATURE_CAMERA_ANY` absent) → skip camera,
  go straight to picker.

## BR2-7 Review (Q2=A)
- Review shows the auto-cropped square preview. **Retake** → back to
  camera/picker. **Accept** → Pick size.

## BR2-8 Generating screen (Q4=A)
- Always show the Generating screen with the blueprint animation for a minimum
  ~500 ms, then navigate to Board when the import completes (whichever is later).

## BR2-9 Delete (Q7=A) — reuses Phase-1 cleanup
- Deleting a custom puzzle shows a **confirmation dialog** ("Delete this puzzle?
  This can't be undone."). On confirm → `PuzzleRepository.deletePuzzle(id)`
  (removes row + SavedBoard/BestScore + image/thumb files). Bundled puzzles are
  not deletable (BR-7).

## BR2-10 Replay at any size (Q2=A, Q3=A)
- A saved custom puzzle can be played at any difficulty from Puzzle Select /
  My-puzzles: re-slice the saved full image for the chosen grid size (no
  re-import needed).

## BR2-11 Resiliency — missing/corrupt custom image
- If a saved custom puzzle's image file is missing on load (BR-8 path):
  `PuzzleFileStore.filesExist` false → the puzzle is shown as unavailable /
  offered for removal; never crashes.

## BR2-12 Security & privacy
- No photo bytes, URIs' contents, or EXIF/PII logged (SECURITY-03).
- Validate the source URI is readable and decodes within caps (SECURITY-05).
- Photos remain in app-internal storage; no network; CAMERA-only permission.
- User-facing errors are generic (SECURITY-09).
