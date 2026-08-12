# Phase 5 — Release & Hardening: Requirements

## Intent Analysis
- **User request**: Proceed to Phase 5 (Release & Hardening).
- **Request type**: Upgrade / Hardening (release engineering).
- **Scope estimate**: Multiple Components — build/signing config, logging,
  dependencies, release verification.
- **Complexity estimate**: Moderate.
- **Target output**: A **signed Play Store AAB** (`app-release.aab`) produced
  from a **newly created release keystore**, with release-safe logging and a
  configured dependency vulnerability scan. **No R8/minification this cycle**
  (not a Play requirement; deferred to a later cycle to minimize first-release
  risk).

## Functional Requirements
- **FR-1 (AAB output):** `./gradlew :app:bundleRelease` produces a signed
  `app-release.aab`.
- **FR-2 (Signing config):** A `release` signing config reads credentials from a
  local, gitignored `keystore.properties` (never committed). If the properties
  file is absent, the release build MUST fail clearly (not silently fall back to
  debug signing) OR skip signing with an explicit message — decided in design.
- **FR-3 (Keystore creation):** Provide the exact `keytool` command and a
  documented procedure for the USER to generate `release.jks` and set the
  passwords. The assistant MUST NOT generate, choose, or store passwords.
- **FR-4 (Version):** `versionName = "1.0.0"`, `versionCode = 1`.
- **FR-5 (Vuln scan):** OWASP Dependency-Check Gradle plugin configured; a
  documented `./gradlew dependencyCheckAnalyze` task produces a CVE report.
- **FR-6 (Release-safe logging):** Review the 5 existing log/println/
  printStackTrace call sites; ensure no secrets/PII are logged and that verbose
  logging is stripped or guarded in release builds (SECURITY-03/09).
- **FR-7 (.gitignore):** Ensure `*.jks`, `keystore.properties` (and existing
  `*.keystore`, `/local.properties`) are gitignored so no signing material is
  ever committed.

## Non-Functional Requirements
- **NFR-1 (No secrets in VCS):** No keystore, password, or key material in git
  history or working tree (SECURITY-12).
- **NFR-2 (Reproducible build):** Release build succeeds on the pinned JDK 21 /
  AGP toolchain already used for debug.
- **NFR-3 (Dependency pinning):** All deps use the version catalog + committed
  lockfile behavior; no dynamic versions (SECURITY-10).
- **NFR-4 (No functional regression):** All 53 unit/PBT tests still pass; the
  release build runs the app (verified on-device by the user).
- **NFR-5 (Minimal install):** No debug-only tooling, sample content, or
  unused permissions ship in release (SECURITY-09).

## Security Compliance (Baseline extension — enabled)
| Rule | Status | Rationale |
|---|---|---|
| SECURITY-01 Encryption at rest/transit | N/A | Offline app; no network/data store beyond app-private files (OS-encrypted at rest by device). No TLS surface. |
| SECURITY-02 Network intermediary logging | N/A | No servers/LB/CDN/API gateway. |
| SECURITY-03 App-level logging (no secrets/PII) | **Applicable** | FR-6: audit log sites; no PII/secrets in release logs. |
| SECURITY-04 HTTP security headers | N/A | No web endpoints. |
| SECURITY-05 API input validation | N/A | No API. (Photo import already bounds-checks.) |
| SECURITY-06 Least-privilege IAM | N/A | No cloud IAM. |
| SECURITY-07 Network config | N/A | No network infra; app has no INTERNET permission. |
| SECURITY-08 App-level access control | N/A | Single-user local app; no auth/resources-by-ID. |
| SECURITY-09 Hardening/misconfig | **Applicable** | No default creds; release build strips debuggable; no stack traces to users. |
| SECURITY-10 Supply chain | **Applicable** | FR-5 vuln scan; version catalog pinning; no unused deps. |
| SECURITY-11 Secure design | N/A | No auth/payment/rate-limit surface. |
| SECURITY-12 Auth/credential mgmt | **Applicable** | Keystore passwords user-held, gitignored, never in source. |
| SECURITY-13 Integrity verification | Partial/N/A | AAB is Play-signed; no external CDN/deserialization of untrusted input. |
| SECURITY-14 Alerting/monitoring | N/A | No server-side logs to alert on. |
| SECURITY-15 Exception handling / fail-safe | **Applicable** | Release build must not crash-leak; existing error overlays fail closed. |

## Resiliency Compliance (Baseline extension — enabled)
- Applicable items: graceful failure on missing keystore properties (fail clear,
  not silent); release build must not regress the existing recoverable-error
  paths (board error overlay, import failures). Cloud-resiliency items N/A.

## Success Criteria
1. `bundleRelease` yields a signed `app-release.aab` (once the user supplies the
   keystore) — or fails with a clear message if properties are missing.
2. `dependencyCheckAnalyze` runs and reports (0 unfixable criticals, or
   documented).
3. No signing material committed; `.gitignore` covers `*.jks` +
   `keystore.properties`.
4. 53/53 tests pass; lint 0 errors; release build installs & runs on device.
5. versionName 1.0.0.
