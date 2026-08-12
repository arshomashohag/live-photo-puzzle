# Security Test Instructions — Phase 5

Security testing for this offline app centers on **dependency vulnerability
scanning** and **release hardening verification** (Security Baseline extension).

## 1. Dependency Vulnerability Scan (OWASP Dependency-Check)
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
./gradlew dependencyCheckAnalyze
```
- **Report**: `build/reports/dependency-check-report.html` (+ JSON).
- **First run** downloads the full NVD CVE database (multi-minute,
  network-bound). Subsequent runs are incremental.
- **Policy this cycle**: **report-only** — it does not fail the build. Review
  the report before each release; escalate to build-failing on criticals in a
  later hardening cycle if desired.
- **Status**: task registered and wired (verified). Full NVD run is a
  pre-release checklist item, intentionally not executed inside this cycle.

## 2. Release Hardening Verification (SECURITY-09 / -15)
- **Not debuggable**: release build type does not set `debuggable true`
  (default false). Confirm no `android:debuggable` in the merged manifest of a
  release build.
- **Fail-safe signing (SECURITY-15)**: missing `keystore.properties` → unsigned
  release, never silent debug-signing. ✅ verified.
- **No default creds in source (SECURITY-12)**: keystore + passwords are
  user-held and gitignored; `keystore.properties.template` contains only
  `CHANGE_ME` placeholders. ✅ verified via `git check-ignore`.

## 3. Release-Safe Logging (SECURITY-03)
- The 5 `Log.w` sites (SoundPlayer, PhotoImporter×3, PuzzleFileStore) carry
  generic messages — no PII, secrets, or stack traces. ✅ verified (no code
  change needed).

## 4. Supply Chain (SECURITY-10)
- Version catalog (`gradle/libs.versions.toml`) uses **pinned** versions only —
  no dynamic (`+`) versions. ✅ verified.

## Out of scope (N/A for an offline app)
Authentication, authorization, transport security, input-injection against a
server, penetration testing — no network, API, auth, or cloud surface exists.
