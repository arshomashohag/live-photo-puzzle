# Code Generation Plan — Phase 1: Architecture Hardening + Room Persistence

**Unit**: `phase1-persistence`
**Project type**: Brownfield (modify-in-place; never create `X_new.kt` duplicates).
**Workspace root**: /Users/shohag/Desktop/Development/android/live-photo-puzzle
**Code location**: `app/src/main/java/com/tessera/puzzle/…` (app code),
`app/src/test/…` + `app/src/androidTest/…` (tests). Docs summaries →
`aidlc-docs/construction/phase1-persistence/code/`.

This plan is the single source of truth for Code Generation. Each step is
bite-sized and ends verifiable. Tests are generated here and executed in Build
and Test.

## Unit Context
- **Implements**: FR-2 (persistence), NFR-1 (architecture), NFR-8 (PBT), plus the
  applicable Security/Resiliency NFRs.
- **Dependencies**: existing engine (`model/*`), screens, PuzzleCatalog, ImageSlicer.
- **Owned entities**: PuzzleEntity, SavedBoardEntity, BestScoreEntity (Room);
  Settings (DataStore).
- **Reuse**: engine + tests move to `domain/` unchanged; screens adapt to StateFlow.

---

## Step 1: Add dependencies (build config)
- [x] `gradle/libs.versions.toml`: add Hilt 2.52, Room 2.6.1, KSP (matched to
  Kotlin 2.0.20), DataStore 1.1.1, Kotest 5.9.1 (runner-junit5 + property +
  assertions), explicit kotlinx-coroutines; plugin entries for Hilt + KSP.
- [x] `build.gradle.kts` (root): declare Hilt + KSP plugins `apply false`.
- [x] `app/build.gradle.kts`: apply Hilt + KSP; add deps; enable JUnit5 platform
  for Kotest (`testOptions`/`tasks.withType<Test> { useJUnitPlatform() }` scoped
  so existing JUnit4 tests still run — use the JUnit5 vintage engine or keep
  JUnit4 tests on JUnit4). Set Room `ksp { arg("room.schemaLocation", …) }` and
  `room.incremental`. Add `@HiltAndroidApp` app class to manifest.

## Step 2: Application class + manifest
- [x] Create `TesseraApplication.kt` (`@HiltAndroidApp`).
- [x] `AndroidManifest.xml`: set `android:name=".TesseraApplication"`.
- [x] `MainActivity`: annotate `@AndroidEntryPoint`.

## Step 3: Move engine to `domain/` (reuse, unchanged)
- [x] Move `model/{Difficulty,Puzzle,Scramble,BoardState}.kt` →
  `domain/model/…` (package `com.tessera.puzzle.domain.model`).
- [x] Move tests `test/.../model/{ScrambleTest,BoardStateTest}.kt` →
  `test/.../domain/model/…`; update package/imports.
- [x] Update all references (`data/`, `game/`, `ui/`) to new package.

## Step 4: Domain persistence types + repository interfaces + validator
- [x] Create `domain/model/persistence/{PuzzleRecord,ImageRef,SavedBoard,BestScore,Settings,HomeStats,ThemeMode,PuzzleSource}.kt`.
- [x] Create `domain/repository/{PuzzleRepository,BoardRepository,StatsRepository,SettingsRepository}.kt` (interfaces per business-logic-model.md).
- [x] Create `domain/validation/BoardValidator.kt` (`isValidOrder(order, tileCount)`).

## Step 5: Business-logic PBT (engine) — Kotest
- [x] Create `test/.../domain/EnginePropertiesTest.kt` (Kotest `StringSpec`/
  `FunSpec` with `checkAll`): scramble validity, non-identity, swap involutive,
  swap commutative, solved oracle, placedCount bound, `isValidOrder`. Seeded.
- [x] Keep existing example-based JUnit4 tests (PBT-10 complement).

## Step 6: Room layer — entities, converters, DAOs, database
- [x] `data/db/entity/{PuzzleEntity,SavedBoardEntity,BestScoreEntity}.kt`.
- [x] `data/db/Converters.kt` (IntArray↔String, enums, ImageRef encode/decode).
- [x] `data/db/dao/{PuzzleDao,BoardDao,StatsDao}.kt` (Flow + suspend; parameterized).
- [x] `data/db/TesseraDatabase.kt` (`@Database`, version 1, exportSchema true).

## Step 7: Mappers + seeder + file store
- [x] `data/mapper/EntityMappers.kt` (entity↔domain).
- [x] `data/db/BundledPuzzleSeeder.kt` (idempotent 9-row seed from PuzzleCatalog).
- [x] `data/files/PuzzleFileStore.kt` (filesDir/puzzles read/write/delete; used in P2).

## Step 8: Repository implementations
- [x] `data/repository/{PuzzleRepositoryImpl,BoardRepositoryImpl,StatsRepositoryImpl}.kt` (suspend/Flow, IO dispatcher, error-boundary, BR-4/5/7/8 logic).
- [x] `data/settings/SettingsRepositoryImpl.kt` (DataStore Preferences).

## Step 9: Persistence PBT + Room tests
- [x] `test/.../data/MapperPropertiesTest.kt` (Kotest): SavedBoard round-trip,
  best-time minimization, order validation (PBT-02/03).
- [x] `androidTest/.../data/PuzzlePersistenceTest.kt` (Room in-memory): seed
  idempotence, save/load board, recordCompletion best-time + solvedCount,
  delete cascade + file cleanup, corrupt-order discard.

## Step 10: Hilt modules (`di/`)
- [x] `di/DatabaseModule.kt` (provides DB + DAOs), `di/RepositoryModule.kt`
  (binds impls→interfaces), `di/DispatcherModule.kt` (@IoDispatcher/@DefaultDispatcher qualifiers), `di/DataStoreModule.kt`.

## Step 11: Migrate GameViewModel → @HiltViewModel + StateFlow + debounced save
- [x] Rewrite `game/GameViewModel.kt` as `@HiltViewModel`, inject repositories +
  dispatchers. Expose `StateFlow<HomeUiState>` (puzzles, continue, stats) and
  `StateFlow<BoardUiState>` (board, tiles, timer). Implement debounced save
  pipeline (debounce 750ms + forced saves) and completion recording. Preserve
  existing engine calls.
- [x] Add UI state models `presentation/HomeUiState.kt`, `presentation/BoardUiState.kt`.

## Step 12: Adapt screens to StateFlow + wire lifecycle saves
- [x] `TesseraApp.kt`: obtain `hiltViewModel()`; pass state down.
- [x] `HomeScreen`: read real stats + continue from state; show corrupt-data
  notice (BR-8). Keep `data-testid`-equivalent stable semantics tags where UI
  is interactive.
- [x] `BoardScreen`: collect BoardUiState; forced-save on Pause/onStop
  (`LifecycleEventEffect`/`DisposableEffect`); tap → VM.
- [x] `CompleteScreen`: read BestScore from state.
- [x] `PuzzleSelectScreen`: show resume indicator if a SavedBoard exists.

## Step 13: Documentation summaries
- [x] `aidlc-docs/construction/phase1-persistence/code/implementation-summary.md`
  (files created/modified, mapping to BR/NFR, test inventory).

## Story / Requirement Traceability
- FR-2 → Steps 4,6,7,8,11,12 · NFR-1 → 3,4,8,10 · NFR-8/PBT → 5,9 ·
  SECURITY-05/15 → 4,8 (validation, error boundary) · Resiliency R-1/R-2 →
  8,11,12 (durable + corrupt discard + notice).

## Scope / Estimated
- 13 steps; ~30 files (create) + ~8 (modify). Tests: 2 PBT specs + 1 Room
  instrumented test + retained JUnit4 examples.
