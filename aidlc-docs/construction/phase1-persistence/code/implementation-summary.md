# Implementation Summary — Phase 1: Architecture Hardening + Room Persistence

Brownfield changes (modify-in-place; no duplicate files). Application code under
`app/src/…`; this doc is the only markdown summary.

## Created — application code
- `TesseraApplication.kt` (`@HiltAndroidApp`)
- `domain/model/persistence/PersistenceModels.kt` (PuzzleRecord, ImageRef,
  SavedBoard, BestScore, Settings, HomeStats, enums)
- `domain/repository/Repositories.kt` (4 repository interfaces)
- `domain/validation/BoardValidator.kt`
- `data/db/entity/Entities.kt` (Puzzle/SavedBoard/BestScore entities)
- `data/db/dao/Daos.kt` (PuzzleDao, BoardDao, StatsDao)
- `data/db/TesseraDatabase.kt`
- `data/db/BundledPuzzleSeeder.kt`
- `data/mapper/EntityMappers.kt`
- `data/files/PuzzleFileStore.kt`
- `data/repository/{PuzzleRepositoryImpl,BoardRepositoryImpl,StatsRepositoryImpl}.kt`
- `data/settings/SettingsRepositoryImpl.kt`
- `di/{DispatcherModule,DatabaseModule,RepositoryModule}.kt`
- `presentation/UiState.kt` (HomeUiState, BoardUiState, CompleteUiState, etc.)

## Created — tests
- `test/domain/EnginePropertiesTest.kt` (Kotest PBT: engine invariants)
- `test/data/MapperPropertiesTest.kt` (Kotest PBT: mapping round-trip)
- `androidTest/data/PuzzlePersistenceTest.kt` (Room in-memory: save/load,
  best-score, delete cascade, corrupt discard)

## Modified
- `gradle/libs.versions.toml`, `build.gradle.kts`, `app/build.gradle.kts`
  (Hilt, Room, KSP, DataStore, Kotest, coroutines, lifecycle-runtime-compose;
  minSdk 29; JUnit5 platform + vintage engine; Room schema export)
- `AndroidManifest.xml` (`android:name=".TesseraApplication"`)
- `MainActivity.kt` (`@AndroidEntryPoint`)
- `game/GameViewModel.kt` → `@HiltViewModel`, StateFlow UI state, debounced +
  forced autosave, repository-backed completion/continue
- `TesseraApp.kt` (shared activity-scoped `hiltViewModel()`; new screen args)
- `ui/screens/{HomeScreen,BoardScreen,CompleteScreen,PuzzleSelectScreen}.kt`
  (collect StateFlow; real stats/continue/best; lifecycle forced-save;
  tile semantics; resume indicator; restore notice)
- Engine files relocated `model/` → `domain/model/` (unchanged logic)

## Requirement / BR / NFR mapping
- **FR-2 persistence** → entities/DAOs/DB, repositories, seeder, VM.
- **BR-1 seed** → BundledPuzzleSeeder (idempotent, non-deletable).
- **BR-2 debounced autosave** → VM `saveRequests.debounce(750)` + `flushSave()`
  on ON_STOP / Pause / exit; forced save on new-board.
- **BR-3 continue** → `BoardDao.observeMostRecent()` → HomeUiState.
- **BR-4/5 completion/best** → StatsRepositoryImpl strict-less-than time.
- **BR-7 delete + cleanup** → PuzzleRepositoryImpl (deletable-only, cascade,
  PuzzleFileStore.deleteFiles).
- **BR-8 corrupt discard + notice** → BoardValidator + toDomainOrNull;
  restoreNotice on Home.
- **NFR S-05/15** → validation boundary, parameterized Room, error-safe repos,
  resource-safe file store. **S-03** no PII/photo logging.
- **PBT** → EnginePropertiesTest + MapperPropertiesTest (Kotest, seeded).

## Verification (executed in Build and Test stage)
- `:app:assembleDebug` + `:app:testDebugUnitTest` (Kotest PBT + JUnit4 examples).
- Room schema exported to `app/schemas/…/1.json`.
- Instrumented `PuzzlePersistenceTest` runs on device/emulator.
