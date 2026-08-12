# Unit Test Execution — Phase 5

## Run Unit Tests
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
./gradlew :app:testDebugUnitTest
```

## Review Test Results
- **Expected**: **53 tests pass, 0 failures, 0 errors** (verified this cycle).
- **Report Location**:
  - JUnit XML: `app/build/test-results/testDebugUnitTest/`
  - HTML: `app/build/reports/tests/testDebugUnitTest/index.html`
- **Coverage note**: pure decision cores (`HintState`, `FeedbackDecider`,
  `ImageSlicer.tileBounds`) are covered by Kotest **property-based** tests
  (`checkAll`), not just examples — per the Property-Based Testing (Full)
  extension.

## Lint
```bash
./gradlew :app:lintDebug
```
- **Expected**: **0 errors** (verified this cycle).
- **Report**: `app/build/reports/lint-results-debug.html`.

## Fix Failing Tests
If any test fails:
1. Open the HTML report to find the failing case (property tests print the
   shrunk counterexample seed).
2. Fix the offending code (the pure cores are deterministic — reproduce with
   the printed seed).
3. Rerun `:app:testDebugUnitTest` until green.
