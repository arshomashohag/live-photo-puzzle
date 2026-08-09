# Build Instructions

## Prerequisites
- **Build Tool**: Gradle (wrapper `./gradlew`, Gradle 8.9), Android Gradle Plugin 8.6.1
- **JDK**: 21 (pinned via `org.gradle.java.home` in `gradle.properties`; AGP 8.6 does not support JDK 26)
- **Android SDK**: platform-android-35, build-tools 35.0.0, platform-tools
- **Environment**: `ANDROID_HOME` pointing at the SDK (e.g. `/opt/homebrew/share/android-commandlinetools`); `local.properties` sets `sdk.dir`
- **System**: macOS/Linux/Windows; ~2 GB free for build cache

## Build Steps

### 1. Install dependencies
Dependencies resolve automatically from Google + Maven Central on first build
(Hilt, Room, DataStore, Kotest, Compose BOM). No manual install.

### 2. Configure environment
```bash
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
```
`local.properties` (auto, not committed) contains `sdk.dir=$ANDROID_HOME`.

### 3. Build
```bash
./gradlew :app:assembleDebug
```

### 4. Verify build success
- **Expected**: `BUILD SUCCESSFUL`
- **Artifact**: `app/build/outputs/apk/debug/app-debug.apk`
- **Room schema** exported to `app/schemas/com.tessera.puzzle.data.db.TesseraDatabase/1.json`
- **Acceptable warnings**: Gradle deprecation notices; none block the build.

## Troubleshooting

### Build fails with a JDK/AGP compatibility error
- **Cause**: A JDK newer than AGP supports (e.g. 26) is used.
- **Solution**: Ensure `org.gradle.java.home` in `gradle.properties` points to a
  JDK 17–21 install (this repo pins JDK 21).

### `SDK location not found`
- **Cause**: Missing `ANDROID_HOME` / `local.properties`.
- **Solution**: Set `ANDROID_HOME` and ensure `local.properties` has `sdk.dir`.

### KSP / Hilt codegen errors after a schema change
- **Cause**: Stale generated code.
- **Solution**: `./gradlew clean` then rebuild.
