# Target API 36 Upgrade — Design

**Date:** 2026-08-15
**Status:** Approved (design); pending implementation
**Scope:** Raise Tessera's target API to 36 (Android 16) to satisfy Google
Play's target-API requirement, via a minimal build-toolchain bump. Ships as
version 1.0.1.

---

## 1. Goal

Google Play requires apps to target within one year of the latest Android
release. From **Aug 31, 2026**, an app whose target API is not within that
window can no longer be updated. Tessera 1.0.0 shipped on **targetSdk 35**
(Android 15). This upgrade moves it to **targetSdk 36** (Android 16) so
future updates remain possible.

The published 1.0.0 app is unaffected and stays live; this only concerns
future updates.

Explicitly **not** in scope:
- Refreshing Compose / Hilt / Room / Kotlin / CameraX versions.
- Enabling R8 / minification (still deferred).
- Adding Play Games Services (declined — it requires the INTERNET
  permission and sign-in, which conflict with Tessera's fully-offline,
  no-data-collection posture).

---

## 2. Current state (verified)

- **AGP** 8.6.1, **Gradle** 8.9, **Kotlin** 2.0.20, **KSP** 2.0.20-1.0.25.
- `compileSdk` / `targetSdk` **35**; `minSdk` **29**.
- Installed SDK platforms: android-34, android-35 (36 **not** installed).
- Installed build-tools: 34.0.0, 35.0.0.
- Key libs: Compose BOM 2024.09.03, Hilt 2.52, Room 2.6.1, CameraX 1.3.4,
  Material3 Adaptive 1.0.0.
- Build env: Temurin JDK 21 toolchain, Java-17 bytecode. Local builds use
  `~/.gradle/gradle.properties` to pin JDK 21; CI uses setup-java.
- `versionCode = System.getenv("VERSION_CODE")?.toIntOrNull() ?: 1`,
  `versionName = System.getenv("VERSION_NAME") ?: "1.0.0"`.

---

## 3. The compatibility crux

Compiling against **SDK 36 requires a newer AGP** than 8.6.1. Setting
`compileSdk = 36` on AGP 8.6.1 fails with a "requires Android Gradle plugin
version …" error. The minimum safe AGP for API 36 support is the **8.9.x**
line.

- **AGP 8.6.1 → 8.9.1.** AGP 8.9.x is compatible with the existing
  **Gradle 8.9** and **Kotlin 2.0.20 / KSP 2.0.20-1.0.25**, so no cascading
  bump of Gradle, Kotlin, or KSP is required. This keeps the blast radius
  to a single toolchain version and the SDK level.

If, during implementation, AGP 8.9.1 turns out to require a Gradle bump
(e.g. Gradle 8.11), that is a small, contained follow-on — update the
wrapper `distributionUrl` and re-run. It does not change the design.

---

## 4. Changes

### 4.1 `gradle/libs.versions.toml`
- `agp = "8.6.1"` → `agp = "8.9.1"`.
- No other version entries change.

### 4.2 `app/build.gradle.kts`
- `compileSdk = 35` → `compileSdk = 36`.
- `targetSdk = 35` → `targetSdk = 36`.
- `minSdk` unchanged (29).
- `versionCode` / `versionName` env-override logic unchanged (a release is
  cut by tagging `v1.0.1`, which the existing `release.yml` turns into
  versionName 1.0.1 + run-number versionCode).

### 4.3 SDK platform
- Install **SDK Platform 36** locally:
  `sdkmanager "platforms;android-36" "build-tools;36.0.0"`.
- CI: the Ubuntu runner's Android SDK / `setup-gradle` provisions the
  required platform automatically; if not, the build step installs it.

### 4.4 `CHANGELOG.md`
- Add a `## [1.0.1]` section documenting the target-API-36 update.

### 4.5 Docs
- `RELEASE_CHECKLIST.md`: note that target API 36 is now met.

---

## 5. Android 16 (API 36) behavior changes to verify

targetSdk 36 opts the app into Android 16 runtime behavior changes. Those
relevant to a small, offline, single-Activity Compose app:

1. **Edge-to-edge enforced (highest risk).** On API 36 apps can no longer
   opt out of edge-to-edge. Compose content may draw under the status/nav
   bars. Verify the splash, home, difficulty, board/HUD, and settings
   drawer handle `WindowInsets` correctly (no clipped or hidden controls).
   If a fix is needed it is a Compose insets adjustment, not a
   dependency change.
2. **Stricter background/foreground-service limits.** Expected **N/A** —
   Tessera has no services or background work.
3. **16 KB native page-size alignment.** The app's `.so` files come from
   AndroidX dependencies (CameraX, DataStore, graphics-path). Recent
   AndroidX releases are aligned; verify the build produces no page-size
   warnings. No first-party native code exists.

Any required fix is expected to be a small Compose/UI adjustment (most
likely insets), captured as its own task in the implementation plan.

---

## 6. Verification strategy

1. **Automated, in this environment:**
   - Install SDK Platform 36.
   - `./gradlew :app:assembleRelease` (or `assembleDebug`) builds green.
   - `./gradlew :app:testDebugUnitTest` → all 53 tests pass.
   - `./gradlew :app:lintDebug` → 0 errors (review any new API-36 lint
     warnings).
2. **On-device (maintainer):** install the debug APK and exercise the key
   flows, watching for edge-to-edge / inset issues:
   - Launch / splash → home
   - Difficulty select
   - **Camera capture → create a custom puzzle**
   - Board: swap tiles, hint, solve reveal, results
   - Settings drawer (theme, sound, haptics)
3. **Release:** only after device sign-off, bump the changelog to 1.0.1,
   tag `v1.0.1`, and let `release.yml` build the signed AAB.

---

## 7. Rollback

All changes are version strings and one SDK-level bump in two files. If the
upgrade proves problematic, revert `libs.versions.toml` and
`app/build.gradle.kts` to the 1.0.0 values (AGP 8.6.1, SDK 35). The
published 1.0.0 is unaffected throughout.

---

## 8. Out of scope (YAGNI)

- Compose BOM / Hilt / Room / Kotlin / KSP / CameraX version bumps.
- R8 / minification (remains deferred).
- Play Games Services (declined).
- Any new features or UI changes beyond what an API-36 behavior change
  strictly requires.
