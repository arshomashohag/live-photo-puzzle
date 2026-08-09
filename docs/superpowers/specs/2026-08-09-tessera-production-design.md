# Tessera — Production-Ready Photo Puzzle Game (Design)

**Date:** 2026-08-09
**Status:** Approved for phased planning
**Supersedes:** `2026-08-09-tessera-core-slice-design.md` (the slice is Phase 0 — already built)
**Source requirements:** "Build a Production-Ready Android Photo Puzzle Game" (24-section spec) + `Tessera Photo Puzzle.dc.html` UI/UX design.

## Summary

Tessera is a production-ready Android photo-puzzle game. Players solve
photo puzzles at three difficulties (3×3 / 4×4 / 5×5) using a **sliding-swap**
mechanic, play nine bundled puzzles, and — the differentiating feature — create
their own puzzles from the camera or the system photo picker, saved locally and
replayable/deletable. The app is offline-first, privacy-first (photos never
leave the device), accessible, adaptive across phone/tablet and light/dark, and
built to current Google Play publication standards.

This document defines the target architecture and decomposes the work into
**seven phases**, each of which produces working, testable software and gets its
own implementation plan.

## Decisions (locked)

- **Mechanic — "sliding swap":** tap tile A then tile B; the two animate/slide
  into each other's positions and exchange. Logic is swap-tile (every
  permutation solvable — no 15-puzzle parity trap); the sliding is a UI
  animation layer over the existing tested engine. This keeps gameplay forgiving
  while giving the tactile feel of sliding.
- **Custom-puzzle image storage:** processed full image + thumbnail written to
  **app-internal storage** (`filesDir/puzzles/<id>.jpg`, `<id>_thumb.jpg`); a
  **Room** row stores paths + metadata. Private, no permissions, removed on
  uninstall; explicit file cleanup on delete.
- **DI:** **Hilt** (`@HiltAndroidApp`, `@HiltViewModel`, `@Inject`).
- **State:** presentation state exposed as **`StateFlow`** from ViewModels
  (migrating the slice's `mutableStateOf`), unidirectional data flow.
- **Camera:** **CameraX**. **Photo picker:** `ActivityResultContracts.PickVisualMedia`
  (no storage permission).
- **Persistence of prefs/settings:** **DataStore (Preferences)**.
- **Language/UI:** Kotlin, Jetpack Compose, Material 3.

## Target Architecture

Layered, with the game engine fully decoupled from Android:

```
:app
 ├─ ui/            Compose screens, theme, primitives, navigation
 ├─ presentation/  ViewModels (StateFlow), UI state models, events
 ├─ domain/        PURE Kotlin: puzzle engine, models, use-cases
 │                 (no Android imports — JVM-unit-testable)
 ├─ data/          Room (entities, DAO, DB), repository impls,
 │                 file storage, DataStore settings
 ├─ image/         Bitmap decode/downsample/crop/slice/thumbnail
 ├─ camera/        CameraX controller + permission handling
 └─ di/            Hilt modules
```

- **domain** has no Android dependency and holds `Difficulty`, `Puzzle`,
  `BoardState`, scramble, solvability, completion — unit-tested on the JVM.
- **data** exposes repositories (`PuzzleRepository`, `SettingsRepository`,
  `StatsRepository`) behind interfaces defined in domain; Hilt binds impls.
- **presentation** ViewModels depend only on repository interfaces + domain.
- **ui** is state-driven; no business logic.

Whether this becomes Gradle multi-module or package-layered within `:app` is an
implementation detail decided in Phase 1's plan; the boundaries above are the
contract either way. Given the size, **package-layered within `:app`** is the
default (keeps Hilt/Compose config simple) unless Phase 1 finds a reason to split.

## Screens (full set, from the UI/UX design)

Launch/browse: Splash, Home, Difficulty, Puzzle select. Create: Camera, Review
capture, Pick size, Generating. Play: Board, Pause, Complete. Library: My
puzzles, Delete confirm, Library empty. Settings. Edge states: Permission
denied, Generation failed, First launch. Plus dark-theme and tablet variants of
the primary screens (adaptive, not separate hardcoded layouts).

## Phases

Each phase is independently shippable and testable. Phase 0 is done.

### Phase 0 — Playable slice (DONE)
Splash → Home → Difficulty → Puzzle select → Board (swap) → Pause → Complete,
nine bundled photos, tested engine. Basis for everything else.

### Phase 1 — Architecture hardening + persistence foundation
**Goal:** production architecture skeleton and durable storage, no new user
features yet.
- Introduce Hilt; add `domain/ data/ presentation/ image/` package structure;
  move engine into `domain`, define repository interfaces.
- Migrate `GameViewModel` to `@HiltViewModel` exposing `StateFlow` UI state.
- Room DB: `PuzzleEntity`, `StatsEntity`/`BestScoreEntity`; DAOs; `PuzzleRepository`,
  `StatsRepository`. Bundled puzzles represented uniformly with custom ones.
- DataStore `SettingsRepository` (sound/haptics/theme prefs; consumed in Phase 4/3).
- Persist in-progress board + best time/moves; **Continue and stats survive
  restart** (replacing the slice's in-memory-only behavior).
- Tests: repository logic (Room in-memory/Robolectric), migrated engine tests.

### Phase 2 — Custom photo puzzles (the differentiator)
**Goal:** create → play → save → delete from camera or picker, fully offline.
- CameraX capture flow with runtime permission handling (rationale, denial,
  permanently-denied → settings deep link, no-camera fallback to picker).
- `PickVisualMedia` photo-picker path (no storage permission).
- Robust **image processing**: `inSampleSize` downsampling to a target
  edge, center-crop to square, tile slicing, thumbnail generation; bounded
  memory, `OutOfMemoryError`/corrupt-image handling, resource release. Off-main
  via coroutines.
- Review (retake/accept) → pick size → Generating → Board.
- Save to Room + `filesDir`; My-puzzles library grid; delete with confirm sheet
  and **file cleanup**.
- Screens: Camera, Review, Pick size, Generating, My puzzles, Delete confirm,
  Library empty, Permission denied, Generation failed.
- Tests: image-processing unit tests (sampling math, crop bounds, corrupt
  input), repository create/delete + file-cleanup tests.

### Phase 3 — Adaptive UI, dark theme, accessibility
**Goal:** the UI meets the spec's device/theme/a11y bar.
- Dark theme (spec's dark palette) via theme-aware color scheme; optional
  dynamic color on Android 12+.
- Adaptive layouts using `WindowSizeClass` — phone/tablet, aspect ratios,
  cutouts/system bars; remove fixed screen dimensions; board sizes to available
  space.
- Font-scaling safe (no clipped fixed heights); reduced-motion honored.
- Accessibility: content descriptions, semantic labels, focus order, ≥48dp
  targets, contrast, non-color-only status (already partly done via meters);
  board tiles get meaningful semantics ("tile 3, selected").
- Tests: a few Compose UI tests for key screens; manual device matrix pass.

### Phase 4 — Sliding-swap animation, audio, haptics, settings
**Goal:** feel + user-controllable feedback.
- **Sliding-swap animation:** animate the two tiles exchanging positions
  (position animation), respecting reduced-motion.
- Sound effects (tile move, completion) and haptics (move, complete, buttons),
  all **optional and Settings-controlled**, honoring system settings.
- Settings screen wired to DataStore (sound, haptics, theme, reset stats).
- Tests: settings persistence; animation gated by reduced-motion.

### Phase 5 — Error handling, security, performance
**Goal:** production robustness.
- User-friendly error states for every failure mode (camera, picker, invalid
  image, processing, storage, generation, DB, corrupt saved puzzle, unexpected);
  never expose stack traces.
- Security: release-safe logging (no photo/PII logging; strip debug logs in
  release), input validation, secure local storage, no secrets, dependency
  review.
- Performance: verify downsampling/no-OOM on high-res, smooth board rendering,
  minimize recomposition, bounded caching, startup, camera init/teardown.
- Release build config: R8/minify, resource shrinking, `debuggable false`,
  signing-config scaffolding (no keys committed), **AAB** output.

### Phase 6 — Documentation, compliance, release readiness, test breadth
**Goal:** satisfy the spec's documentation and acceptance sections.
- **Research current Google Play requirements** (target API, permissions,
  photo/video access, User Data / Data safety, privacy policy, AAB, signing)
  before writing compliance claims.
- Docs: `README.md`, `ARCHITECTURE.md`, `PRIVACY.md`,
  `PLAY_STORE_COMPLIANCE.md`, `RELEASE_CHECKLIST.md`, and a
  `KNOWN_LIMITATIONS.md`.
- Broaden tests toward the spec's unit + UI matrix; run lint/static analysis to
  passing; document the device-testing matrix and results.
- Final acceptance pass against Section 24, documenting any remaining limitation.

## Cross-Cutting Requirements (apply to every phase)

- Offline-first: no feature requires network; **no `INTERNET` permission**.
- Least-privilege permissions: only **CAMERA**, requested only at capture.
- No main-thread blocking; image/DB work on coroutines/dispatchers.
- No hardcoded secrets; no sensitive/photo logging.
- No placeholder presented as complete (the current "Create from camera →
  Coming soon" is removed in Phase 2 when the real flow lands; until then it is
  documented as a known limitation, not shipped as done).
- Lint/static checks kept green.

## Testing Strategy

- **domain** (JVM unit): scramble validity/solvability, swap, completion,
  timer/score, difficulty config. **data**: repository + Room + file cleanup.
  **image**: sampling/crop/thumbnail + corrupt input. **presentation**:
  ViewModel state transitions. **ui** (instrumented/Compose): home, difficulty,
  select, gameplay, pause/resume, complete, library, delete, settings; camera
  flow where practical.
- Device/manual matrix: small/standard/large phone, tablet, densities, font
  scales, light/dark, offline, camera-denied, picker scenarios.

## Non-Goals

- No cloud, accounts, analytics, crash-reporting SDKs, or ads (privacy-first).
  If ever added, they trigger new Data-safety declarations — out of scope here.
- No classic 15-puzzle gap mechanic (superseded by sliding-swap).

## Risks / Open Questions

- **Java 26 vs AGP:** builds pinned to JDK 21 (works today); revisit if AGP
  upgraded.
- **Bundled puzzles in Room:** need a seed/migration that represents the 9
  bundled puzzles as rows without duplicating the drawables — resolved in Phase 1
  plan (seed-on-first-run referencing drawable ids).
- **Robolectric vs instrumented for data tests:** chosen in Phase 1 plan.
- **Play compliance is time-sensitive:** claims deferred to Phase 6 after live
  verification, per the spec's explicit instruction not to claim compliance
  unverified.
