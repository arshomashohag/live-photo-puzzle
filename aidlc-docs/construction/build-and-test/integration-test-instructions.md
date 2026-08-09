# Integration Test Instructions

Phase 1 is a single-module app; "integration" here means the persistence layer
working end-to-end with the ViewModel and the real Room database. There are no
separate services to integrate.

## Scenario 1: Repository ↔ Room (data layer)
- **Description**: Repositories read/write through real DAOs and the mapper.
- **Covered by**: `androidTest/.../data/PuzzlePersistenceTest.kt` (in-memory
  Room). Verifies save/load round-trip, most-recent Continue selection,
  best-time recording (strict-less-than + solvedCount), delete cascade with
  bundled-reject, and corrupt-order discard.
- **Run**:
  ```bash
  ./gradlew :app:connectedDebugAndroidTest
  ```
  Requires a connected device/emulator.
- **Cleanup**: in-memory DB is torn down per test (`@After db.close()`).

## Scenario 2: ViewModel ↔ repositories (restart survival) — manual
- **Description**: Continue card, stats, and best-time survive process death.
- **Steps** (on device):
  1. Start a puzzle, make a few moves, background the app (Home button).
  2. From Android Settings → Developer options, or `adb shell am kill
     com.tessera.puzzle`, kill the process.
  3. Relaunch. Confirm the Home **Continue** card shows the in-progress board and
     resumes at the same placement; stats reflect prior solves.
- **Expected**: state restored from Room (no reset).

## Setup
No external services, containers, or network. The Room DB and DataStore are
app-internal; airplane mode may be ON for all scenarios (offline-first).
