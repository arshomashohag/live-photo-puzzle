# Unit Test Execution

## Run Unit Tests (JVM — no device)

### 1. Execute all unit tests
```bash
./gradlew :app:testDebugUnitTest
```
Runs on the JUnit Platform: **Kotest** property-based specs + **JUnit4** example
tests (via the JUnit vintage engine).

### 2. Review results
- **Expected**: 19 tests pass, 0 failures.
- **Report**: `app/build/reports/tests/testDebugUnitTest/index.html`
- **XML**: `app/build/test-results/testDebugUnitTest/`

Suites:
| Suite | Type | Tests |
|---|---|---|
| `domain.EnginePropertiesTest` | Kotest PBT | 6 |
| `data.MapperPropertiesTest` | Kotest PBT | 3 |
| `domain.model.BoardStateTest` | JUnit4 example | 6 |
| `domain.model.ScrambleTest` | JUnit4 example | 4 |

### 3. Reproducibility (PBT-08)
Kotest logs the seed on failure; rerun a single spec with:
```bash
./gradlew :app:testDebugUnitTest --tests "com.tessera.puzzle.domain.EnginePropertiesTest"
```

### 4. Fix failing tests
Review `index.html`, fix the code, rerun until green.

## Instrumented Tests (Room — needs device/emulator)
```bash
./gradlew :app:connectedDebugAndroidTest
```
Runs `data.PuzzlePersistenceTest` (in-memory Room): save/load round-trip,
most-recent selection, best-time recording, delete cascade + bundled-reject,
corrupt-order discard. Requires a connected device or running emulator
(`adb devices` must list one).
