# Functional Design Plan — Phase 1: Architecture Hardening + Room Persistence

**Unit**: `phase1-persistence`

## Plan Checklist
- [ ] Define domain entities (persistence-facing): PuzzleRecord, SavedBoard, PuzzleStats/BestScore, Settings
- [ ] Define Room schema (entities, keys, indices, relationships) mapped from domain
- [ ] Define domain↔entity mapping (round-trip) and repository interfaces
- [ ] Define business rules: best-score (lowest time), bundled-puzzle seeding, completion recording, in-progress persistence, delete + file cleanup
- [ ] Define data flow: app start → seed → load Continue/stats; play → autosave board; complete → record best; delete → row + files
- [ ] Identify PBT-01 testable properties (engine invariants + mapping round-trips)
- [ ] Collect answers to embedded questions; resolve ambiguities
- [ ] Generate business-logic-model.md, business-rules.md, domain-entities.md
- [ ] Security/Resiliency/PBT compliance summary in completion message

---

## Clarifying Questions

Answer each after the `[Answer]:` tag; choose "Other" and describe if none fit.
(Most technical decisions are already locked in requirements.md — these are
functional/schema details only.)

## Question 1: In-progress board autosave frequency
When should the in-progress board be persisted so it survives process death?

A) On every move (each swap) — most durable, tiny writes; guarantees no lost progress

B) On pause and on app background/stop (onStop) only — fewer writes, may lose the last few moves if the process is killed without onStop

C) Debounced (e.g. batch writes at most every ~2s) plus on background — balance of durability and write volume

X) Other (please describe after [Answer]: tag below)

[Answer]:

## Question 2: How many in-progress boards are retained?
The Continue card resumes an in-progress puzzle. How many can be "in progress" at once?

A) One global in-progress board (starting a new puzzle replaces it) — matches the current single Continue card

B) One in-progress board per puzzle+difficulty (resume any previously-started puzzle where you left off)

X) Other (please describe after [Answer]: tag below)

[Answer]:

## Question 3: Best-score scope (the key the record is kept against)
Best time is tracked per…

A) Per puzzle + difficulty (e.g. "Ridgeline at Medium" has its own best) — a puzzle played at different sizes keeps separate bests

B) Per difficulty only (best time for any 3×3, any 4×4, any 5×5)

C) Both: per-puzzle+difficulty best AND a per-difficulty overall best

X) Other (please describe after [Answer]: tag below)

[Answer]:

## Question 4: Home stats strip (currently hardcoded "18 / 00:41 / 7")
The Home stats strip should show real data once persistence exists. What should the three cells be?

A) SOLVED (total puzzles solved) · BEST 3×3 (best easy time) · CREATED (count of custom puzzles) — matches the current labels

B) SOLVED · BEST (overall best time across all) · PLAYED (total games played)

C) Keep the current labels but I'll refine exact metrics later — wire whatever is cleanest now

X) Other (please describe after [Answer]: tag below)

[Answer]:

## Question 5: Corrupt / missing saved data handling (Resiliency)
If a saved board or puzzle row is corrupt or its image file is missing on load, the app should…

A) Silently discard the bad record (drop the Continue card / skip the puzzle) and continue — never crash, no user-facing error for this background case

B) Discard it but surface a small, friendly non-blocking notice ("Couldn't restore your last puzzle")

X) Other (please describe after [Answer]: tag below)

[Answer]:

## Question 6: Bundled puzzles in the database
Bundled puzzles need to be represented uniformly with custom ones for stats/best-score. How?

A) Seed 9 bundled-puzzle rows on first launch (referencing drawable ids; no image files copied) — DB migration/seed on first run; bundled rows are non-deletable

B) Keep bundled puzzles purely in code (PuzzleCatalog) and store only *stats/best-score* rows keyed by puzzle id in the DB (don't put bundled puzzles themselves in the puzzle table)

X) Other (please describe after [Answer]: tag below)

[Answer]:
