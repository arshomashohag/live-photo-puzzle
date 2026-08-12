# Phase 5 — Code Generation Summary

## What was built
- **`.gitignore`**: added `*.jks` and `keystore.properties` (kept existing
  `*.keystore`, `*.aab`, `/local.properties`). Verified via `git check-ignore`.
- **`keystore.properties.template`** (committed, no secrets): documents the four
  signing keys the build reads.
- **`app/build.gradle.kts`**:
  - Loads `keystore.properties` from repo root **if present** (else empty).
  - `signingConfigs { create("release") { ... } }` populated only when the file
    exists.
  - `release` build type: `isMinifyEnabled = false` (R8 deferred; not a Play
    requirement) and `signingConfig = <release> if props exist else null` — so a
    missing keystore yields an **unsigned** release rather than silent
    debug-signing.
  - `versionName = "1.0.0"` (versionCode 1).
- **`gradle/libs.versions.toml`** + **root `build.gradle.kts`**: OWASP
  Dependency-Check plugin (`dependencyCheck = "10.0.4"`), applied at root.
  Task: `./gradlew dependencyCheckAnalyze` (report-only).
- **Release-safe logging (verified, no change)**: the 5 `Log.w` sites
  (SoundPlayer, PhotoImporter×3, PuzzleFileStore) carry generic messages with no
  PII, secrets, or stack traces → SECURITY-03/09 satisfied without churn.
- **`docs/RELEASE_SIGNING.md`**: user-run `keytool` procedure (keytool prompts
  for passwords — never generated/held by tooling), template-copy steps, build
  commands, dep-scan command, keystore-loss warning, R8-off note.

## Verification (evidence)
- `bundleRelease` with **no** keystore → BUILD SUCCESSFUL, `app-release.aab`
  produced (unsigned) — proves the fail-open-to-unsigned path.
- `bundleRelease` with a **throwaway** keystore → `app-release.aab` contains
  `META-INF/TESSERA.RSA`/`.SF` (properly signed with alias `tessera`). Throwaway
  `.jks` + `keystore.properties` deleted immediately; `git status` confirms
  neither is tracked.
- `:app:testDebugUnitTest` → **53/53 pass**.
- `:app:lintDebug` → **0 errors**.
- `dependencyCheckAnalyze` task is registered (root); running it downloads the
  NVD CVE feed (long, network-bound) — documented for the release checklist
  rather than run inside this cycle.

## Security compliance (Baseline)
| Rule | Status |
|---|---|
| SECURITY-03 logging | Compliant (no PII/secrets/stack traces) |
| SECURITY-09 hardening | Compliant (no default creds in source; release not debuggable; no stack traces to users) |
| SECURITY-10 supply chain | Compliant (pinned catalog; OWASP scan wired; no dynamic versions) |
| SECURITY-12 credentials | Compliant (keystore + passwords user-held, gitignored, never in source) |
| SECURITY-15 fail-safe | Compliant (missing keystore → unsigned, not silent debug-sign) |
| SECURITY-01/02/04/05/06/07/08/11/13/14 | N/A (offline app; no network/API/auth/cloud) |

## Out of scope (as planned)
- No R8/minify/shrink (deferred).
- No Room removal (still used by custom-puzzle storage).
- Assistant never created a real keystore or password.
