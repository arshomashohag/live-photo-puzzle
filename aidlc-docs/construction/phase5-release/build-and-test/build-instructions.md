# Build Instructions — Phase 5 (Release)

## Prerequisites
- **Build Tool**: Gradle (wrapper) with Android Gradle Plugin 8.6.1
- **JDK**: 21 (`/usr/libexec/java_home -v 21`)
- **Android SDK**: `ANDROID_HOME=/opt/homebrew/share/android-commandlinetools`
  (compileSdk/targetSdk 35, minSdk 29)
- **Signing (release only)**: a `keystore.properties` at repo root + the
  referenced `.jks`. Both are gitignored. See `docs/RELEASE_SIGNING.md`.
- **System**: macOS/Linux, ~2 GB free for build cache + AAB output.

## Build Steps

### 1. Environment
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
```

### 2. Debug build (no signing needed)
```bash
./gradlew :app:assembleDebug
```

### 3. Release AAB (Play Store artifact)
```bash
./gradlew :app:bundleRelease
```
- **With** `keystore.properties` present → **signed** AAB (upload to Play).
- **Without** it → build still succeeds but the AAB is **unsigned** (safe for
  a fresh clone / CI; cannot be uploaded to Play). This is a deliberate
  fail-open-to-unsigned design, never silent debug-signing.

### 4. Verify Build Success
- **Expected Output**: `BUILD SUCCESSFUL`
- **Artifact**: `app/build/outputs/bundle/release/app-release.aab`
  (~14.6 MB as of this build).
- **Signed check**: `unzip -l app-release.aab | grep META-INF` should list
  `TESSERA.RSA` / `TESSERA.SF` when signed.

## Troubleshooting

### `SDK location not found`
- **Cause**: `ANDROID_HOME` unset or `local.properties` missing.
- **Solution**: export `ANDROID_HOME` as above (or set `sdk.dir` in
  `local.properties`, which is gitignored).

### Release AAB builds but is unsigned
- **Cause**: no `keystore.properties` at repo root.
- **Solution**: follow `docs/RELEASE_SIGNING.md` to create the keystore and
  `keystore.properties`, then rebuild.

### `Unsupported class file major version`
- **Cause**: wrong JDK.
- **Solution**: ensure `JAVA_HOME` points at JDK 21.
