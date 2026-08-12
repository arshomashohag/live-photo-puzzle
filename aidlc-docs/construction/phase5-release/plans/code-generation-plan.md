# Phase 5 — Code Generation Plan

> Part 1 (this doc) = the plan for your approval. Part 2 = execution.
> Each step ends [ ]/[x] for tracking. No R8/minification this cycle.

## Overview
Turn the debug-only build into a release-capable one that produces a **signed
Play Store AAB**, add a **dependency vulnerability scan**, verify **release-safe
logging**, and ensure **no signing material can ever be committed** — without
the assistant ever generating or holding a password.

## Findings that shape the plan (verified)
- `app/build.gradle.kts` release block is just `isMinifyEnabled = false`; no
  `signingConfig`, no `bundle {}`, `versionName="1.0"`, `versionCode=1`.
- All 5 log call sites are generic `Log.w` (no PII/secrets/stack traces) →
  SECURITY-03/09 already satisfied; FR-6 becomes verify + a release-safe note,
  not a rewrite.
- `.gitignore` has `*.keystore` + `/local.properties` but NOT `*.jks` or
  `keystore.properties` → must add.
- Version catalog uses pinned versions (no dynamic) → SECURITY-10 pinning OK.

---

## Step 1: .gitignore — never commit signing material
- [x] Add to `.gitignore`:
  ```
  # Release signing material — NEVER commit
  *.jks
  keystore.properties
  ```
  (Keeps existing `*.keystore`, `/local.properties`.)

## Step 2: keystore.properties template (committed) + real file (gitignored)
- [x] Create `keystore.properties.template` (committed, no secrets) documenting
  the four keys the build expects:
  ```properties
  # Copy to keystore.properties (gitignored) and fill in.
  # Generate the keystore with the keytool command in
  # docs/RELEASE_SIGNING.md. NEVER commit the real file or the .jks.
  storeFile=release.jks
  storePassword=CHANGE_ME
  keyAlias=tessera
  keyPassword=CHANGE_ME
  ```
- [x] Do NOT create the real `keystore.properties` or any `.jks` — the USER does
  that (Step 6 doc). The assistant never generates/holds passwords (SECURITY-12).

## Step 3: app/build.gradle.kts — signing config + AAB + version
- [x] At the top of `app/build.gradle.kts` (after plugins), load the properties
  file if present:
  ```kotlin
  import java.util.Properties
  import java.io.FileInputStream

  val keystorePropsFile = rootProject.file("keystore.properties")
  val keystoreProps = Properties().apply {
      if (keystorePropsFile.exists()) load(FileInputStream(keystorePropsFile))
  }
  ```
- [x] Add a `signingConfigs` block inside `android { }`:
  ```kotlin
  signingConfigs {
      create("release") {
          if (keystorePropsFile.exists()) {
              storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
              storePassword = keystoreProps.getProperty("storePassword")
              keyAlias = keystoreProps.getProperty("keyAlias")
              keyPassword = keystoreProps.getProperty("keyPassword")
          }
      }
  }
  ```
- [x] Wire it into the release build type; assign the signing config ONLY when
  the properties exist so a fresh checkout still builds an (unsigned) release
  and CI without the keystore fails clear rather than silently debug-signing:
  ```kotlin
  buildTypes {
      release {
          isMinifyEnabled = false
          signingConfig = if (keystorePropsFile.exists())
              signingConfigs.getByName("release") else null
      }
  }
  ```
- [x] Bump version: `versionName = "1.0.0"` (versionCode stays 1).

## Step 4: OWASP Dependency-Check plugin (SECURITY-10)
- [x] Add the plugin + version to `gradle/libs.versions.toml`:
  ```toml
  # [versions]
  dependencyCheck = "10.0.4"
  # [plugins]
  owasp-dependencycheck = { id = "org.owasp.dependencycheck", version.ref = "dependencyCheck" }
  ```
- [x] Apply it (report-only, non-failing this cycle) in the root
  `build.gradle.kts`:
  ```kotlin
  plugins {
      // ...existing apply false lines...
      alias(libs.plugins.owasp.dependencycheck)
  }
  ```
- [x] Task to run: `./gradlew dependencyCheckAnalyze` → HTML/JSON report under
  `build/reports/dependency-check-report.html`. (Documented in build-and-test.)

## Step 5: Release-safe logging verification (SECURITY-03/09)
- [x] Confirm (already true) the 5 `Log.w` sites carry no PII/secrets/stack
  traces. No code change required. Record the verification in the code summary.
  (Rationale: rewriting benign generic warnings adds risk for no security gain;
  a non-debuggable release build does not expose these to users.)

## Step 6: docs/RELEASE_SIGNING.md — keystore procedure (user-run)
- [x] Create `docs/RELEASE_SIGNING.md` with:
  - The exact `keytool` command for the USER to generate the keystore:
    ```bash
    keytool -genkeypair -v \
      -keystore release.jks \
      -alias tessera \
      -keyalg RSA -keysize 4096 -validity 10000
    ```
    (keytool prompts for the passwords — the USER chooses and stores them; they
    are never passed on the command line or seen by the assistant.)
  - Instructions to copy `keystore.properties.template` → `keystore.properties`
    and fill in the four values.
  - The build/verify commands (`bundleRelease`) and a warning that losing the
    keystore means you can never update the app on Play.

---

## Files touched
| File | Change |
|------|--------|
| `.gitignore` | add `*.jks`, `keystore.properties` |
| `keystore.properties.template` | **new** (committed, no secrets) |
| `app/build.gradle.kts` | signing config, release signingConfig wiring, versionName 1.0.0 |
| `gradle/libs.versions.toml` | OWASP dependency-check version + plugin |
| `build.gradle.kts` (root) | apply OWASP plugin |
| `docs/RELEASE_SIGNING.md` | **new** keystore procedure (user-run) |

## Out of scope (explicit)
- No R8/minify/shrink (not a Play requirement; deferred to a later cycle).
- No Room removal (still used by custom-puzzle storage; unrelated to release).
- Assistant does NOT create the real keystore or any password.

## Verification (Part 2 / Build and Test)
1. Generate a **throwaway** test keystore locally to prove `bundleRelease`
   signs and outputs `app-release.aab`; then delete it (never committed).
2. `./gradlew :app:testDebugUnitTest` → 53/53 pass.
3. `./gradlew :app:lintDebug` → 0 errors.
4. `./gradlew dependencyCheckAnalyze` → report generated.
5. `git status` shows no `.jks` / `keystore.properties` tracked.
