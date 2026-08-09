# Business Logic Model — Phase 1 Persistence

## Repository Interfaces (domain-defined, data-implemented)

```
interface PuzzleRepository {
    suspend fun ensureSeeded()                       // BR-1
    fun observePuzzles(): Flow<List<PuzzleRecord>>
    suspend fun getPuzzle(id: String): PuzzleRecord?
    suspend fun addCustomPuzzle(record: PuzzleRecord) // Phase 2 use
    suspend fun deletePuzzle(id: String)              // BR-7 (+ file cleanup)
}

interface BoardRepository {
    suspend fun loadBoard(puzzleId: String, difficulty: Difficulty): SavedBoard?
    suspend fun saveBoard(board: SavedBoard)          // BR-2 (debounced/forced by caller)
    suspend fun clearBoard(puzzleId: String, difficulty: Difficulty)
    fun observeMostRecent(): Flow<SavedBoard?>        // BR-3 (Continue)
    suspend fun boardsForPuzzle(puzzleId: String): List<SavedBoard>
}

interface StatsRepository {
    suspend fun recordCompletion(
        puzzleId: String, difficulty: Difficulty,
        elapsedMillis: Long, moves: Int,
    )                                                 // BR-4/BR-5
    suspend fun bestScore(puzzleId: String, difficulty: Difficulty): BestScore?
    fun observeHomeStats(): Flow<HomeStats>           // BR-6
}

interface SettingsRepository {
    val settings: Flow<Settings>
    suspend fun setSoundEnabled(enabled: Boolean)
    suspend fun setHapticsEnabled(enabled: Boolean)
    suspend fun setTheme(mode: ThemeMode)
}
```

## Key Data Flows

### DF-1 App start
`ensureSeeded()` (BR-1) → observePuzzles + observeMostRecent (Continue) +
observeHomeStats feed the Home StateFlow. Corrupt/missing records handled per
BR-8 (discard + notice).

### DF-2 Start / resume a board
Open (puzzleId, difficulty) → `loadBoard`. If present and valid → resume; else
create fresh scramble (existing engine) → `saveBoard`. Validation per BR-8.

### DF-3 Play (debounced autosave)
`tap(pos)` → engine `BoardState.tapTile` (unchanged) → new state → **schedule a
debounced save** (~750 ms; rapid taps coalesce, BR-2). Forced immediate save on
onStop / Pause / before completion. Off main thread (BR-9).

### DF-4 Completion
Engine reports `isSolved` → `recordCompletion` (BR-4) → `clearBoard` (DF-2 row
removed) → Complete screen reads the BestScore.

### DF-5 Delete (custom; Phase 2 exercises fully)
`deletePuzzle(id)` → reject if bundled (BR-7) → delete SavedBoard + BestScore
rows → delete image/thumb files → observers refresh.

## ViewModel state (presentation)
`GameViewModel` migrates to `@HiltViewModel`, injects the repositories, and
exposes `StateFlow<HomeUiState>` and `StateFlow<BoardUiState>` (UDF). The pure
engine (`domain/`) is unchanged; the ViewModel orchestrates engine + repos.

---

## Testable Properties (PBT-01) — MANDATORY

Framework: **Kotest Property Testing** (Kotlin). Seeded/reproducible (PBT-08);
complements example-based tests (PBT-10).

### Engine (pure domain) — adjacent-only (edge-sharing) swap rule
| Property | Category | Statement |
|---|---|---|
| Scramble validity | Invariant | For all perfect-square tileCounts, `scramble(n)` is a permutation of `0..n-1`. |
| Scramble non-identity | Invariant | For all seeds, `scramble(n)` ≠ identity. |
| Scramble solvable | Verification | The board is solvable using only legal edge-sharing swaps (proved constructively by snake-path bubble sort). |
| Adjacent swap involutive | Round-trip | Swapping an edge-sharing pair (a,b) twice returns the original `order`. |
| Non-adjacent tap re-selects | Invariant | Tapping a non-adjacent tile while one is selected moves the selection and never mutates `order`. |
| placedCount bound | Invariant | `placedCount` ∈ `0..tileCount`, equals tileCount iff solved. |

**Rule note (CR):** swaps are restricted to orthogonal edge-sharing neighbors
(no diagonals, no wrap) — a selected tile can swap with at most 4 (interior),
3 (edge), or 2 (corner) tiles. Because adjacent transpositions generate the full
symmetric group, every board remains solvable; scrambles are generated via
random adjacent swaps to keep difficulty controlled and avoid near-solved starts.

### Persistence mapping (data)
| Property | Category | Statement |
|---|---|---|
| SavedBoard entity round-trip | Round-trip (PBT-02) | `toDomain(toEntity(board)) == board` for all valid boards (order/selected/moves/elapsed preserved). |
| BestScore upsert idempotence | Idempotence | Recording the *same* completion twice yields solvedCount+? — see note; best time/moves unchanged after the min is set for equal/greater times. |
| Best-time minimization | Invariant | After a sequence of completions, `bestTimeMillis` = min of all recorded times; `bestMoves` = moves of that min run. |
| Order validation | Invariant | `isValidOrder(order, tileCount)` true iff `order` is a permutation of `0..tileCount-1`. |

*Note on idempotence:* `recordCompletion` increments `solvedCount` each call by
design (it counts solves), so it is **not** idempotent on count — the idempotent
property is specifically on `bestTimeMillis`/`bestMoves` for a non-improving
time. This is stated precisely to avoid a false PBT.

### Components with no PBT properties
- Repository I/O wiring, Hilt modules, DataStore plumbing — **No PBT properties
  identified** (thin integration/glue; covered by example-based Room tests).
