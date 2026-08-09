# Tech-Stack Decisions — Phase 2 Custom Photo Puzzles

Additive to the Phase-1 stack (Kotlin, Compose, Hilt, Room, DataStore, StateFlow,
Kotest). New dependencies pinned in the version catalog (SECURITY-10).

| Concern | Choice | Version (target) | Rationale |
|---|---|---|---|
| Camera preview + capture | **CameraX** (`camera-core`, `camera-camera2`, `camera-lifecycle`, `camera-view`) | 1.3.4 | Lifecycle-aware, handles device quirks; ImageCapture only (Q1=A). |
| Photo picker | `ActivityResultContracts.PickVisualMedia` | AndroidX Activity (already present) | System picker, no storage permission. |
| EXIF orientation | `androidx.exifinterface` | 1.3.7 | Read rotation to upright the bitmap before slice (Q2=A). |
| Permission flow | `rememberLauncherForActivityResult(RequestPermission)` | Activity Compose (present) | CAMERA request at capture time. |
| Image decode/scale | Android `BitmapFactory` + pure `ImageMath` | platform | Downsample via inSampleSize; no extra image lib needed. |
| PBT | Kotest (present) | 5.9.1 | Properties for pure ImageMath / naming (PBT-09 satisfied in Phase 1). |

## Manifest additions
- `<uses-permission android:name="android.permission.CAMERA" />`
- `<uses-feature android:name="android.hardware.camera.any" android:required="false" />`
  (camera optional — app still works via picker on camera-less devices).
- Still **no `INTERNET`**.

## Notes
- No new image-loading library (Coil/Glide) — bundled drawables use
  `painterResource`; custom files decoded directly. Keeps deps minimal.
- CameraX capture writes to app **cache**; temp file deleted after import
  (Q3=A).
