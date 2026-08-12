# Integration Test Instructions — Phase 5

## Applicability
Tessera is a **single-module, offline** Android app. Phase 5 changes are
release-engineering only (signing config, versionName, dependency-scan plugin,
gitignore, docs) — no new services, APIs, or cross-service interactions.

Traditional service-to-service integration tests are therefore **N/A**. The
meaningful "integration" for this phase is that the release **build pipeline**
produces a valid, signed, installable artifact. That is verified below and
end-to-end on a real device.

## Release-Pipeline Verification (the phase's integration surface)

### Scenario 1: Unsigned release path (fresh clone / CI)
- **Setup**: no `keystore.properties` at repo root.
- **Steps**: `./gradlew :app:bundleRelease`.
- **Expected**: `BUILD SUCCESSFUL`; `app-release.aab` produced but **unsigned**.
- **Verified**: ✅ this cycle.

### Scenario 2: Signed release path (release machine)
- **Setup**: create a keystore + `keystore.properties` per
  `docs/RELEASE_SIGNING.md` (a throwaway keystore was used for this verification
  and deleted immediately).
- **Steps**: `./gradlew :app:bundleRelease`; then
  `unzip -l app/build/outputs/bundle/release/app-release.aab | grep META-INF`.
- **Expected**: AAB contains `META-INF/TESSERA.RSA` and `TESSERA.SF`.
- **Verified**: ✅ this cycle (throwaway key; then deleted; `git status` confirms
  no signing material tracked).

### Scenario 3: On-device end-to-end (user)
- **Setup**: install the debug/release build on a physical device.
- **Steps**: play a puzzle → solve → observe 2s full-image reveal → Complete
  screen; use Hint (overlay aligns exactly with the grid, timer keeps running,
  count decrements, disables at 0).
- **Expected**: features behave as designed; no visual jump on hint overlay.
- **Verified**: ✅ confirmed by the user on their phone.

## Cleanup
No services to tear down. If a throwaway keystore was created for signing
verification, delete `release.jks` and `keystore.properties` (never commit them).
