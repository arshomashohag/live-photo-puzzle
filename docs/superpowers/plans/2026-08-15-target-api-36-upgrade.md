# Target API 36 Upgrade Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Raise Tessera to targetSdk 36 (Android 16) so it stays updatable on Google Play, via a minimal build-toolchain bump, shipped as 1.0.1.

**Architecture:** Bump AGP from 8.6.1 to 8.9.1 (the floor that can compile SDK 36), install SDK Platform 36, set compileSdk/targetSdk to 36. Gradle 8.9, Kotlin 2.0.20, and KSP stay unchanged (AGP 8.9.x is compatible with them). Verify with a green build + all unit tests + lint here, plus a maintainer device check for Android 16 edge-to-edge behavior. No other dependency or feature changes.

**Tech Stack:** Android Gradle Plugin, Gradle (version catalog), Jetpack Compose, JDK 21 toolchain / Java-17 bytecode.

**Spec:** `docs/superpowers/specs/2026-08-15-target-api-36-upgrade-design.md`

## Global Constraints

- **Build env:** Temurin JDK 21. Local builds resolve JDK 21 via the untracked `~/.gradle/gradle.properties` pin; run gradle with `ANDROID_HOME=/opt/homebrew/share/android-commandlinetools`. Do NOT re-add `org.gradle.java.home` to the repo.
- **Minimal scope:** Only `agp`, `compileSdk`, `targetSdk`, and the SDK platform change. Do NOT bump Compose BOM, Hilt, Room, Kotlin, KSP, or CameraX.
- **minSdk stays 29.** versionCode/versionName env-override logic stays as-is.
- **No R8, no Play Games Services.**
- **Quality bars (must all pass before release):** `:app:assembleRelease` builds; `:app:testDebugUnitTest` = 53/53 pass; `:app:lintDebug` = 0 errors.
- **Commit style:** Commitizen, no Claude co-author, stage specific files only (never `git add .`), never `--no-verify`.
- **Branch:** do this work on a feature branch off `main` (e.g. `chore/target-api-36`), not directly on `main`.

## Pre-flight (not a task): create the branch

Before Task 1, from an up-to-date `main`:

```bash
git checkout main && git pull --ff-only
git checkout -b chore/target-api-36
```

---

### Task 1: Install SDK Platform 36

The build cannot compile against SDK 36 until the platform is installed. This is an environment step with no repo change, but it gates every later task.

**Files:** none (SDK install only).

**Interfaces:**
- Consumes: nothing.
- Produces: `android-36` platform + `build-tools;36.0.0` available under `ANDROID_HOME`, so later Gradle builds can resolve `compileSdk = 36`.

- [ ] **Step 1: Install the platform and build-tools**

Run:

```bash
ANDROID_HOME=/opt/homebrew/share/android-commandlinetools \
  /opt/homebrew/share/android-commandlinetools/cmdline-tools/latest/bin/sdkmanager \
  --install "platforms;android-36" "build-tools;36.0.0"
```

Accept the license prompt if shown (type `y`).

- [ ] **Step 2: Verify it installed**

Run:

```bash
ls /opt/homebrew/share/android-commandlinetools/platforms
```

Expected: the listing now includes `android-36` (alongside android-34, android-35).

---

### Task 2: Bump AGP to 8.9.1

AGP 8.6.1 cannot compile against SDK 36. Bumping AGP first (before the SDK level) isolates any toolchain incompatibility from the SDK change. If AGP 8.9.1 demands a newer Gradle, this task surfaces it in isolation.

**Files:**
- Modify: `gradle/libs.versions.toml:2`

**Interfaces:**
- Consumes: SDK platform 36 from Task 1 (not strictly needed to build at SDK 35, but the branch is mid-upgrade).
- Produces: AGP 8.9.1 in use; the project still targets SDK 35 at this point, so it must still build green — proving the AGP bump alone is clean before the SDK level changes.

- [ ] **Step 1: Change the AGP version**

In `gradle/libs.versions.toml`, change line 2:

```toml
agp = "8.6.1"
```

to:

```toml
agp = "8.9.1"
```

- [ ] **Step 2: Build with the new AGP (still targeting SDK 35)**

Run:

```bash
ANDROID_HOME=/opt/homebrew/share/android-commandlinetools ./gradlew :app:assembleDebug --no-daemon
```

Expected: `BUILD SUCCESSFUL`. If it fails complaining about the Gradle version (e.g. "Android Gradle plugin requires Gradle X.Y"), update the wrapper: edit `gradle/wrapper/gradle-wrapper.properties` `distributionUrl` to the required version (e.g. `gradle-8.11.1-bin.zip`), then re-run this step. Record the Gradle bump in the commit if one was needed.

- [ ] **Step 3: Run the unit tests to confirm the toolchain is healthy**

Run:

```bash
ANDROID_HOME=/opt/homebrew/share/android-commandlinetools ./gradlew :app:testDebugUnitTest --no-daemon
```

Expected: all 53 tests pass, 0 failures.

- [ ] **Step 4: Commit**

```bash
git add gradle/libs.versions.toml
git commit -m "build: bump agp to 8.9.1 for android 16 support"
```

(If a Gradle wrapper bump was needed in Step 2, add `gradle/wrapper/gradle-wrapper.properties` to this commit and mention it in the message, e.g. `build: bump agp to 8.9.1 and gradle to 8.11.1 for android 16 support`.)

---

### Task 3: Raise compileSdk and targetSdk to 36

With AGP 8.9.1 in place and SDK 36 installed, move the SDK levels. This is the change that actually satisfies Play's requirement and opts the app into Android 16 behavior.

**Files:**
- Modify: `app/build.gradle.kts:23` (compileSdk), `app/build.gradle.kts:28` (targetSdk)

**Interfaces:**
- Consumes: AGP 8.9.1 (Task 2), SDK platform 36 (Task 1).
- Produces: an app compiled against and targeting API 36. `minSdk` stays 29.

- [ ] **Step 1: Change compileSdk**

In `app/build.gradle.kts`, change:

```kotlin
    compileSdk = 35
```

to:

```kotlin
    compileSdk = 36
```

- [ ] **Step 2: Change targetSdk**

In `app/build.gradle.kts`, change:

```kotlin
        targetSdk = 35
```

to:

```kotlin
        targetSdk = 36
```

(Leave `minSdk = 29` unchanged.)

- [ ] **Step 3: Build the release bundle**

Run:

```bash
ANDROID_HOME=/opt/homebrew/share/android-commandlinetools ./gradlew :app:bundleRelease --no-daemon
```

Expected: `BUILD SUCCESSFUL`. (Unsigned locally, since `keystore.properties` is absent — that is fine; CI signs on tag.) Watch the output for any new API-36 warnings (e.g. 16 KB page-size alignment); note them but they are not build failures.

- [ ] **Step 4: Run unit tests and lint**

Run:

```bash
ANDROID_HOME=/opt/homebrew/share/android-commandlinetools ./gradlew :app:testDebugUnitTest :app:lintDebug --no-daemon
```

Expected: 53/53 tests pass; lint reports 0 errors. Review any new lint warnings introduced by API 36; address only if they are errors.

- [ ] **Step 5: Commit**

```bash
git add app/build.gradle.kts
git commit -m "build: target android 16 (api 36)"
```

---

### Task 4: Verify Android 16 edge-to-edge on device (maintainer)

targetSdk 36 enforces edge-to-edge. The app already calls `enableEdgeToEdge()` in `MainActivity` and applies `windowInsetsPadding(WindowInsets.safeDrawing)` on every content screen (Board, Home, Difficulty, Complete, PuzzleSelect, MyPuzzles, Create*, Settings drawer). Splash intentionally draws full-bleed. So this task is **verification**, expected to require no code change — but it is the gate that catches any inset regression the automated build cannot.

**Files:** none expected. If a gap is found, modify the specific screen to add `.windowInsetsPadding(WindowInsets.safeDrawing)` (matching the existing pattern in e.g. `CompleteScreen.kt:48`).

**Interfaces:**
- Consumes: the API-36 build from Task 3.
- Produces: confirmation (or a targeted inset fix) that the UI is correct under Android 16.

- [ ] **Step 1: Build and install the debug APK**

Run:

```bash
ANDROID_HOME=/opt/homebrew/share/android-commandlinetools ./gradlew :app:installDebug --no-daemon
```

Expected: `BUILD SUCCESSFUL` and the app installs on the connected device. (Maintainer runs this with a phone connected.)

- [ ] **Step 2: Exercise each flow, watching for clipped/hidden UI at screen edges**

On the device, check that no control is hidden under the status or navigation bars on:
- Splash → Home
- Difficulty select
- Camera capture → create a custom puzzle
- Board: swap tiles, use a hint, solve, see the reveal + results
- Settings drawer (theme, sound, haptics)

Expected: all controls fully visible and tappable; content not obscured by system bars.

- [ ] **Step 3: (Only if a gap is found) fix the affected screen**

For any screen where content is clipped by a system bar, add safe-drawing insets to its root layout, matching the existing pattern:

```kotlin
Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
    // existing content
}
```

Then rebuild (Step 1), re-verify (Step 2), and commit:

```bash
git add app/src/main/java/com/tessera/puzzle/ui/screens/<Screen>.kt
git commit -m "fix(ui): apply safe-drawing insets on <screen> for android 16"
```

If no gap is found, no commit is made for this task.

---

### Task 5: Update CHANGELOG and release checklist

Record the 1.0.1 release and that target API 36 is now met. Folded into one task because both are small documentation edits gated together.

**Files:**
- Modify: `CHANGELOG.md`
- Modify: `RELEASE_CHECKLIST.md`

**Interfaces:**
- Consumes: the completed upgrade (Tasks 2–3).
- Produces: a `1.0.1` changelog section (which `release.yml` will pull into the GitHub Release when `v1.0.1` is tagged) and an updated checklist note.

- [ ] **Step 1: Add a 1.0.1 section to CHANGELOG.md**

Insert directly under the `# Changelog` header block, above the `## [1.0.0]` section:

```markdown
## [1.0.1] - 2026-08-15

### Changed
- Target Android 16 (API level 36) to meet Google Play's target-API
  requirement; upgraded the Android Gradle Plugin to 8.9.1. No user-facing
  feature or behaviour changes.
```

- [ ] **Step 2: Note target-36 in RELEASE_CHECKLIST.md**

In `RELEASE_CHECKLIST.md`, under the `## Notes` section, add:

```markdown
- **Target API:** the app targets Android 16 (API 36) as of 1.0.1, meeting
  Play's target-API requirement (deadline Aug 31, 2026).
```

- [ ] **Step 3: Verify the changelog extraction still works for 1.0.1**

Run (mirrors the `release.yml` awk extraction):

```bash
awk -v ver="1.0.1" '
  $0 ~ "^## \\[" ver "\\]" {grab=1; next}
  grab && /^## \[/ {exit}
  grab {print}
' CHANGELOG.md
```

Expected: prints the `### Changed` block for 1.0.1 (non-empty), confirming `release.yml` will find it when `v1.0.1` is tagged.

- [ ] **Step 4: Commit**

```bash
git add CHANGELOG.md RELEASE_CHECKLIST.md
git commit -m "docs: record 1.0.1 target-api-36 release"
```

---

### Task 6: Finalize — merge and release (maintainer-gated)

The upgrade is not "done" until it is on `main` and (optionally) tagged. This is a distinct gate because the release trigger is maintainer-controlled and depends on Task 4's device sign-off.

**Files:** none (git operations only).

**Interfaces:**
- Consumes: Tasks 1–5 complete and green; Task 4 device sign-off received.
- Produces: the upgrade merged to `main`; optionally a `v1.0.1` tag that drives `release.yml`.

- [ ] **Step 1: Push the branch and open a PR to main**

```bash
git push -u origin chore/target-api-36
```

Then open a PR against `main` (compare URL, since `gh` is not installed):
`https://github.com/arshomashohag/live-photo-puzzle/compare/main...chore/target-api-36?expand=1`

- [ ] **Step 2: Confirm CI is green on the PR**

The `ci.yml` workflow runs on the PR. Expected: the `build` job passes (tests + lint) on the Ubuntu runner with SDK 36. If CI cannot find SDK 36, add a platform-install step to `ci.yml` (`sdkmanager "platforms;android-36"`) — but first confirm it actually fails, since `setup-gradle` usually provisions it.

- [ ] **Step 3: Merge to main**

Merge the PR once green (maintainer action).

- [ ] **Step 4: (Optional, when ready to ship) tag the release**

Only after device sign-off (Task 4) and merge:

```bash
git checkout main && git pull --ff-only
git tag v1.0.1
git push origin v1.0.1
```

This triggers `release.yml` → signed AAB + GitHub Release with the 1.0.1 notes. Then upload the AAB to Play (per `RELEASE_CHECKLIST.md`).

---

## Self-Review

**1. Spec coverage:**
- §3 AGP crux / 8.9.1 → Task 2. ✅
- §4.1 catalog agp bump → Task 2. ✅
- §4.2 compileSdk/targetSdk 36 → Task 3. ✅
- §4.3 install SDK 36 → Task 1 (local) + Task 6 Step 2 (CI). ✅
- §4.4 CHANGELOG 1.0.1 → Task 5. ✅
- §4.5 RELEASE_CHECKLIST note → Task 5. ✅
- §5 API-36 behavior (edge-to-edge, background N/A, page-size) → Task 4 (edge-to-edge device check) + Task 3 Step 3 (page-size warnings watched). ✅
- §6 verification (build + 53 tests + lint here; device flows; tag) → Tasks 2/3 (automated), Task 4 (device), Task 6 (tag). ✅
- §7 rollback → covered by the two-file, version-only change surface (Tasks 2–3); no dedicated task needed. ✅
- §8 out of scope → no task bumps other deps, R8, or adds Play Games. ✅

**2. Placeholder scan:** No TBD/TODO/"handle edge cases". Each step has the exact edit or command. The one conditional (Task 2 Gradle bump, Task 4 inset fix) shows the actual code/command to run if the condition occurs. ✅

**3. Type/name consistency:** AGP `8.9.1`, SDK `36`, `build-tools;36.0.0`, and the `~/.gradle` JDK-21 pin are referenced identically across tasks. The changelog header `## [1.0.1]` in Task 5 matches the awk pattern in Task 5 Step 3 and the `release.yml` contract. Screen inset pattern in Task 4 matches the real `CompleteScreen.kt:48` line. ✅
