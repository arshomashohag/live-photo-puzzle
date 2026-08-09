# Domain Entities — Phase 1 Persistence

Technology-agnostic domain model. Room entities and DataStore keys in the data
layer map to/from these; the domain layer has no Android imports.

## Entity: PuzzleRecord
Represents a playable puzzle — bundled or custom — uniformly.

| Field | Type | Notes |
|---|---|---|
| id | String | Stable id. Bundled: `easy-1`…`hard-3`. Custom: UUID. |
| name | String | Display name (e.g. "Ridgeline"). |
| source | PuzzleSource | `BUNDLED` or `CUSTOM`. |
| imageRef | ImageRef | How to load the image (see ImageRef). |
| createdAt | Long (epoch ms) | Bundled: seed time; Custom: capture time. |
| deletable | Boolean | Bundled = false; Custom = true. |

## Value: ImageRef (sealed)
- `DrawableRef(resName: String)` — bundled; resolved to a drawable id at the UI
  boundary (stored as the resource *name*, not the numeric id, so it survives
  R-class changes).
- `FileRef(imagePath: String, thumbPath: String)` — custom; app-internal file
  paths under `filesDir/puzzles/`.

*(Phase 1 only exercises `DrawableRef` — the 9 bundled puzzles. `FileRef` is
defined now so the schema is stable for Phase 2, but no custom puzzles are
created yet.)*

## Entity: SavedBoard
A persisted in-progress game. **One per (puzzleId, difficulty)** (Q2=B).

| Field | Type | Notes |
|---|---|---|
| puzzleId | String | FK → PuzzleRecord.id. |
| difficulty | Difficulty | EASY/MEDIUM/HARD. |
| order | IntArray | Current tile permutation (the board state). |
| selected | Int? | Currently selected tile position, if any. |
| moves | Int | Move count so far. |
| elapsedMillis | Long | Accumulated play time. |
| updatedAt | Long (epoch ms) | Last autosave; drives "most recent" for Continue. |

Primary key = (puzzleId, difficulty). Solved boards are **not** kept as
SavedBoard — on completion the SavedBoard row is deleted and a BestScore is
recorded.

## Entity: BestScore
Best result for a (puzzleId, difficulty) (Q3=A).

| Field | Type | Notes |
|---|---|---|
| puzzleId | String | FK → PuzzleRecord.id. |
| difficulty | Difficulty | |
| bestTimeMillis | Long | **Lowest** completion time = the ranking metric (Q8=A). |
| bestMoves | Int | Moves of the best-time run (secondary/displayed). |
| solvedCount | Int | Times this (puzzle,difficulty) has been solved. |
| updatedAt | Long (epoch ms) | |

Primary key = (puzzleId, difficulty).

## Value: Settings
Persisted via DataStore (storage only this phase; consumed in Phase 4).

| Field | Type | Default |
|---|---|---|
| soundEnabled | Boolean | true |
| hapticsEnabled | Boolean | true |
| theme | ThemeMode (`SYSTEM`/`LIGHT`/`DARK`) | SYSTEM |

## Derived: HomeStats
Computed (not stored) for the Home stats strip (Q4=A).

| Field | Derivation |
|---|---|
| solvedTotal | Σ BestScore.solvedCount across all records |
| bestEasyTime | min bestTimeMillis where difficulty = EASY (or null) |
| createdCount | count of PuzzleRecord where source = CUSTOM |

## Relationships
```
PuzzleRecord 1 ──< SavedBoard   (0..N; one per difficulty)
PuzzleRecord 1 ──< BestScore    (0..N; one per difficulty)
```
Deleting a PuzzleRecord (custom only) cascades to its SavedBoard and BestScore
rows and deletes its image + thumbnail files.
