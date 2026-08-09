# Requirements — Tessera Production Photo Puzzle Game

## Intent Analysis

- **User request**: Build a complete, production-ready Android photo puzzle game
  from the provided 24-section specification and the Tessera UI/UX design,
  starting from the existing Phase-0 playable slice.
- **Request type**: New Project build-out / Enhancement (brownfield — extends the
  existing slice toward production).
- **Scope estimate**: System-wide (multiple components: engine, persistence,
  camera, image processing, UI, settings, docs/release).
- **Complexity estimate**: Complex (multi-subsystem, production quality bar,
  Google Play compliance).
- **Requirements depth**: Comprehensive.

## Locked Technical Decisions (from prior discussion + clarifying answers)

| Decision | Value |
|---|---|
| Mechanic | **Sliding-swap** — tap two tiles → they animate/slide and exchange; all arrangements solvable (no 15-puzzle parity) |
| Language / UI | Kotlin, Jetpack Compose, Material 3 |
| Architecture | Layered: domain (pure) / data / presentation / image / camera / ui; StateFlow, UDF |
| DI | **Hilt** |
| Persistence | **Room** (metadata) + app-internal files (`filesDir`) for images; **DataStore** for settings |
| Camera | **CameraX** |
| Photo picker | **PickVisualMedia** (no storage permission) |
| Permissions | **CAMERA only**, requested at capture; **no `INTERNET`** |
| Offline | Fully offline-first for all core functionality |
| minSdk | **29** (Android 10) — Q6=C, pinned to the lowest value in the chosen "29 or higher" range |
| compileSdk / targetSdk | Current stable (35), verified against Play target-API requirement in Phase 6 |
| App identity | `com.tessera.puzzle`, display name "Tessera" (Q7=A) |
| Bundled photos | Keep picsum/Unsplash photos; license/attribution documented (Q5=A) |
| Best score | Rank by **lowest completion time**; move count shown as secondary stat (Q8=A) |
| PBT framework | **Kotest Property Testing** |

## Extension Configuration (opt-in answers)

- **Security Baseline**: Enabled (Q1=A). Relevant rules enforced; cloud/web rules N/A per stage.
- **Resiliency Baseline**: Enabled (Q2=A). On-device recoverability/graceful-degradation applied; cloud/DR/HA N/A.
- **Property-Based Testing**: Enabled, Full (Q3=A). Kotest; engine + serialization properties.

## Delivery Approach

- **Q4=B — one phase at a time.** This planning cycle covers **Phase 1**
  (architecture hardening + Room persistence). Later phases each get their own
  Requirements→Design→Code→Test cycle. The overall phase roadmap is recorded for
  context but only Phase 1 is planned/built now.

### Phase roadmap (context; only Phase 1 is in-scope this cycle)
- **P0** Playable slice — DONE.
- **P1** Architecture hardening + persistence (THIS CYCLE).
- **P2** Custom photo puzzles (CameraX + PickVisualMedia + image processing + save/library/delete).
- **P3** Adaptive UI, dark theme, accessibility.
- **P4** Sliding-swap animation, audio, haptics, settings.
- **P5** Error handling, security hardening, performance, release build (R8/AAB/signing).
- **P6** Docs (README, ARCHITECTURE, PRIVACY, PLAY_STORE_COMPLIANCE, RELEASE_CHECKLIST), verified Play compliance, test breadth.

---

## Functional Requirements

### FR-1 Core game (bundled)
- Three difficulties: Easy 3×3 (9), Medium 4×4 (16), Hard 5×5 (25).
- Ship 9 bundled puzzles (3 per difficulty), each a **different** image.
- Sliding-swap gameplay; every generated board is **solvable** and not trivially
  near-solved.
- Board, timer, move counter, pause/resume, restart, completion detection.

### FR-2 Persistence (Phase 1 focus)
- Custom and bundled puzzles represented uniformly in Room.
- Persisted metadata per puzzle: id, image reference, thumbnail reference,
  difficulty, grid size, creation timestamp, completion state, best time, best
  move count, and per-puzzle game statistics where applicable.
- **Best score = lowest completion time** (moves stored/displayed as secondary).
- In-progress board state persisted; **Continue and stats survive app restart /
  process death** (replaces the slice's in-memory-only behavior).
- Settings persisted via DataStore (sound, haptics, theme — consumed in later
  phases; storage introduced in Phase 1).
- Deletion removes the Room row **and** associated image/thumbnail files.

### FR-3 Custom photo puzzles (Phase 2)
- Flow: Camera → Capture → Review (retake/accept) → Select difficulty →
  Generate → Play → Save → later Delete.
- Also support selecting an existing image via the system photo picker.
- Original user photo **never leaves the device**.

### FR-4 Image processing (Phase 2)
- Downsample (bounded memory, `inSampleSize`) to avoid OOM on high-res photos;
  center-crop to square; slice into N×N tiles; generate a thumbnail.
- Handle invalid/corrupt images gracefully; release bitmap resources.

### FR-5 UI (all phases; design fidelity)
- Implement the Tessera blueprint design faithfully across all screens.
- Light + dark themes; adaptive phone/tablet layouts; respect system bars/cutouts.

### FR-6 Settings, audio, haptics (Phase 4)
- Optional tile-move sound, completion sound, button/haptic feedback — all
  Settings-controlled, honoring system preferences; never mandatory.

### FR-7 Error handling (Phase 5)
- User-friendly handling for: camera unavailable/denied, picker failure, invalid
  image, processing failure, storage failure, generation failure, DB failure,
  corrupt saved puzzle, unexpected errors. Never expose raw stack traces.

---

## Non-Functional Requirements

### NFR-1 Architecture & code quality
- Game engine is pure Kotlin, Android-independent, independently unit-testable.
- Clear layer separation; repository pattern; Hilt DI; StateFlow UDF.
- No magic numbers, no hardcoded device dimensions, no placeholder-as-complete.
- Lint/static analysis passing.

### NFR-2 Performance
- No main-thread blocking (image/DB/IO on coroutines/dispatchers).
- Bounded image memory; no unbounded caching; smooth board rendering; minimal
  recomposition; fast startup; correct camera init/teardown.
- Target low/mid-range as well as flagship devices.

### NFR-3 Security (enforced Security Baseline rules)
- **SECURITY-03**: No PII or user-photo data in logs; structured logging;
  release strips debug logs.
- **SECURITY-05**: Validate external/input data (image bytes, DB inputs);
  parameterized DB access (Room enforces).
- **SECURITY-09**: No stack traces/internal details shown to users; use current
  supported SDK & dependency versions; no default credentials/secrets.
- **SECURITY-10**: Dependencies pinned (version catalog) + Gradle lockfile;
  dependency vulnerability scan documented in build instructions; no unused deps.
- **SECURITY-11**: Isolate security-relevant logic (input validation, file/DB
  boundaries) in dedicated modules.
- **SECURITY-15**: All external calls (DB, file I/O, camera) have explicit error
  handling; fail-closed; resources released (bitmaps, cursors, streams) in error
  paths; top-level error handling.
- Cloud/web rules (SECURITY-01/02/04/06/07/08/12/13/14) are **N/A** for an
  offline on-device app.

### NFR-4 Resiliency (enforced, on-device subset)
- Graceful degradation on corrupt image / missing file / DB failure.
- Recoverability: survive process death; corrupt-save detection & recovery.
- Observability: crash-safety without logging PII/photos.
- Cloud/DR/HA practice areas **N/A**.

### NFR-5 Privacy
- Privacy-first: photos remain on-device; no analytics, crash-reporting SDKs, or
  ads. Only CAMERA permission; no network.

### NFR-6 Accessibility (Phase 3)
- TalkBack, content descriptions, semantic labels, focus order, font/display
  scaling, sufficient contrast, ≥48dp targets, reduced-motion, non-color-only
  status indicators.

### NFR-7 Offline-first
- Open app, play bundled puzzles, capture/create/save/play/delete custom
  puzzles — all without internet. No `INTERNET` permission.

### NFR-8 Testing (with PBT)
- **Unit**: puzzle generation, solvability, tile movement, completion, shuffle,
  difficulty config, timer, score, persistence, image-metadata handling.
- **PBT (Kotest, Full)**: scramble = valid permutation & never identity
  (invariant); swap is its own inverse / commutative (round-trip); solved-state
  oracle; entity↔domain mapping round-trip (PBT-02). Seeded/reproducible
  (PBT-08); complements example-based tests (PBT-10).
- **UI**: home, difficulty, selection, gameplay, pause/resume, completion,
  library, delete, settings; camera flow where practical (later phases).
- **Device matrix**: small/standard/large phone, tablet, densities, font scales,
  light/dark, offline, camera-denied, picker scenarios (later phases).

### NFR-9 Build & release (Phase 5/6)
- Version catalogs; separate debug/release; R8/minify + resource shrinking;
  `debuggable false` release; AAB output; signing-config scaffolding with **no
  keys committed**; release-safe logging.

### NFR-10 Google Play compliance (Phase 6)
- Verify **current** Play requirements before claiming compliance (target API,
  permissions, photo/video access, User Data / Data safety, privacy policy, AAB,
  signing). Produce PLAY_STORE_COMPLIANCE.md, PRIVACY.md, RELEASE_CHECKLIST.md.
  **No compliance claims until verified in Phase 6.**

---

## Key Requirements Summary

Tessera is an **offline-first, privacy-first** Android photo puzzle game with a
**sliding-swap** mechanic, 9 bundled puzzles across 3 difficulties, and a
**camera/photo-picker custom-puzzle** feature whose images never leave the
device. It uses a **layered architecture** (pure engine + Room + Hilt +
StateFlow), targets **minSdk 29**, and is built to a production bar with
**enforced Security, Resiliency, and Property-Based-Testing** baselines. Work
proceeds **one phase at a time**; this cycle plans **Phase 1 — architecture
hardening + Room persistence** (durable Continue/stats/best-time surviving
restart), with later phases (custom photos, adaptive/dark/a11y UI,
animation/audio/haptics/settings, error/security/perf/release, docs/compliance)
each getting their own cycle.
