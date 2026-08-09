# Business Rules — Phase 1 Persistence

## BR-1 Bundled-puzzle seeding (Q6=A)
- On first launch (empty DB), seed **9 PuzzleRecord rows** for the bundled
  puzzles from PuzzleCatalog: source = BUNDLED, deletable = false, imageRef =
  DrawableRef(resName).
- Seeding is idempotent: run only when the puzzle table has no BUNDLED rows;
  re-running must not duplicate rows (keyed by stable id).
- Bundled rows are **never deletable** (BR-7 rejects deletion).

## BR-2 In-progress autosave (Q1=A, Q2=B)
- The active board is persisted as a SavedBoard **on every move** (each swap),
  keyed by (puzzleId, difficulty).
- Starting/opening a puzzle+difficulty with an existing SavedBoard **resumes**
  from it; without one, a fresh scrambled board is created and immediately saved.
- `updatedAt` is set on each save.
- Multiple SavedBoards may coexist (one per puzzle+difficulty).

## BR-3 Continue selection
- Home "Continue" surfaces the SavedBoard with the **greatest `updatedAt`**
  (most recently played). If none exist, no Continue card.
- Puzzle Select shows a resume indicator for any puzzle that has a SavedBoard at
  the selected difficulty.

## BR-4 Completion recording (Q3=A, Q8=A)
- When a board becomes solved:
  1. Delete its SavedBoard (a solved board is not "in progress").
  2. Upsert BestScore for (puzzleId, difficulty):
     - Increment `solvedCount`.
     - If no prior best OR `elapsedMillis < bestTimeMillis`: set
       `bestTimeMillis = elapsedMillis`, `bestMoves = moves`.
     - Ranking metric is **lowest time**; `bestMoves` follows the best-time run.
  3. Set `updatedAt`.

## BR-5 Best-score comparison
- Strictly-less-than on time: a tie does not overwrite (keeps the earliest best
  and its move count). Deterministic and idempotent for equal times.

## BR-6 Home stats (Q4=A)
- SOLVED = Σ BestScore.solvedCount.
- BEST 3×3 = min EASY `bestTimeMillis` (display "—" if none).
- CREATED = count of CUSTOM PuzzleRecords (0 in Phase 1; wiring present).

## BR-7 Deletion + file cleanup
- Only CUSTOM (deletable = true) records may be deleted; deleting a BUNDLED
  record is rejected (no-op + logged, never crashes).
- Deleting a record removes: its SavedBoard rows, its BestScore rows, and its
  image + thumbnail **files** (FileRef). File deletion failures are handled
  gracefully (logged without PII, DB row still removed) — SECURITY-15 fail-safe.

## BR-8 Corrupt / missing data (Q5=B, Resiliency)
- On load, if a SavedBoard is malformed (e.g. `order` not a valid permutation
  for the difficulty's tile count) or a referenced image file is missing:
  - Discard the offending record (delete the bad SavedBoard row; skip the
    puzzle), never crash.
  - Surface a small, friendly **non-blocking** notice (e.g. a one-line message
    on Home: "Couldn't restore your last puzzle"). No stack traces (SECURITY-09).
- Validation: a SavedBoard's `order` must contain each index `0..tileCount-1`
  exactly once and `selected` (if set) must be in range.

## BR-9 Persistence threading (NFR-2 / SECURITY-15)
- All DB and file operations run off the main thread (coroutines +
  IO dispatcher). Repository functions are `suspend` or return `Flow`.
- Errors from DB/file calls are caught at the repository boundary; resources
  (cursors/streams) are released; failures degrade gracefully.

## BR-10 No sensitive logging (SECURITY-03)
- No user photo bytes, file contents, or PII are logged. Only non-sensitive
  identifiers/counters may appear in diagnostic logs, and debug logs are
  stripped from release builds (enforced in Phase 5).
