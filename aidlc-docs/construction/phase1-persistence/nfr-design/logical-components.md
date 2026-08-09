# Logical Components — Phase 1 Persistence

Logical (technology-mapped) components and how they wire together. All
on-device; no infrastructure services.

## Component map

```
presentation/
  GameViewModel (@HiltViewModel)
    ├─ collects PuzzleRepository / BoardRepository / StatsRepository / SettingsRepository (Flows)
    ├─ owns the debounced save pipeline (StateFlow + debounce + mapLatest)
    └─ exposes StateFlow<HomeUiState>, StateFlow<BoardUiState>

domain/
  model/         Difficulty, Puzzle, Scramble, BoardState   (moved, unchanged)
  repository/    PuzzleRepository, BoardRepository, StatsRepository, SettingsRepository (interfaces)
  model persistence types: PuzzleRecord, ImageRef, SavedBoard, BestScore, Settings, HomeStats
  validation/    BoardValidator (isValidOrder), ImageRefResolver contract

data/
  db/
    TesseraDatabase (RoomDatabase)
    PuzzleDao, BoardDao, StatsDao
    entities: PuzzleEntity, SavedBoardEntity, BestScoreEntity
    converters: TypeConverters (IntArray<->String, enums, ImageRef)
    seeding: BundledPuzzleSeeder (BR-1)
  repository/
    PuzzleRepositoryImpl, BoardRepositoryImpl, StatsRepositoryImpl (implement domain ports)
  settings/
    SettingsRepositoryImpl (DataStore Preferences)
  files/
    PuzzleFileStore (filesDir/puzzles read/write/delete; Phase-2 heavy use)
  mapper/
    EntityMappers (entity <-> domain; round-trip tested — PBT-02)

di/
  DatabaseModule (provides TesseraDatabase, DAOs)
  RepositoryModule (binds *RepositoryImpl -> interfaces)
  DispatcherModule (@IoDispatcher, @DefaultDispatcher)
  DataStoreModule (provides DataStore<Preferences>)

ui/  (existing screens; consume ViewModel StateFlow)
```

## Component responsibilities

| Component | Responsibility | NFR ties |
|---|---|---|
| `TesseraDatabase` + DAOs | Durable local storage; parameterized queries | RP-1, SP-1 |
| `EntityMappers` | entity↔domain conversion; round-trip integrity | PBT-02 |
| `BoardValidator` | permutation/range validation on read | RP-2, SP-1 |
| `BundledPuzzleSeeder` | idempotent first-run seed of 9 rows | BR-1 |
| `PuzzleFileStore` | app-internal image/thumb file lifecycle + cleanup | RP-3, BR-7 |
| `*RepositoryImpl` | ports→adapters; suspend/Flow; error boundary | PP-1, RP-3, MP-1 |
| `SettingsRepositoryImpl` | DataStore-backed prefs Flow | — |
| Debounced save pipeline (in VM) | coalesce writes; forced saves on lifecycle | PP-2, RP-1 |
| Hilt modules | wire adapters, dispatchers, DB, DataStore | MP-1 |
| Dispatcher qualifiers | confine I/O off main thread | PP-1 |

## Data-store components (no cloud)
- **Room DB** file in app-internal storage (encrypted-at-rest only if device FBE
  applies; no app-managed keys — appropriate for non-sensitive puzzle metadata).
- **DataStore** preferences file (settings).
- **filesDir/puzzles/** for custom images (Phase 2).

There are **no** external infrastructure components (no servers, queues, caches,
load balancers, gateways) — consistent with an offline, single-user app.

## Integration pattern
- **Unidirectional data flow**: DB Flows → repositories → ViewModel StateFlow →
  Compose UI. User events → ViewModel → repositories → DB → (Flow) back to UI.
- **DI**: Hilt constructs the graph at the Application/Activity/ViewModel scopes;
  no manual singletons.
