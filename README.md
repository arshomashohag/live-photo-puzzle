# Tessera

Tessera is an offline photo puzzle game for Android. Slide-scramble a photo
into tiles, solve it, and watch the full image reveal. Play with the built-in
images or create your own puzzle from a photo you capture or import — everything
stays on your device.

## Features

- **Tile puzzles** from built-in photos or your own images
- **Create your own puzzle** — capture with the camera or import a photo
- **Hints** — briefly reveal the full image (limited per game; timer keeps
  running)
- **Solve reveal** — the completed photo animates in before the results screen
- **Accessibility & comfort** — dark mode, reduced-motion support, adaptive
  layout, toggleable haptics and sound

## Privacy at a glance

Tessera is **fully offline**. It has **no internet permission**, no analytics,
no ads, and no accounts. Nothing you do — including photos you use for custom
puzzles — ever leaves your device. See [PRIVACY.md](PRIVACY.md).

## Tech stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Architecture**: unidirectional data flow (StateFlow), pure decision cores
- **DI**: Hilt
- **Storage**: DataStore (settings), Room + app-private files (custom puzzles)
- **Camera**: CameraX
- **Testing**: JUnit + Kotest property-based tests

See [ARCHITECTURE.md](ARCHITECTURE.md) for the layer breakdown.

## Requirements

- **JDK 21** (used by the Gradle toolchain; app compiles to Java 17 bytecode)
- **Android SDK** with platform 35 installed
- **minSdk 29**, **targetSdk / compileSdk 35**

## Build & run

Set the environment (macOS example):

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
```

Debug build / install:

```bash
./gradlew :app:assembleDebug          # build a debug APK
./gradlew :app:installDebug           # install on a connected device
```

Release bundle (Play Store AAB):

```bash
./gradlew :app:bundleRelease
```

Signing is driven by a **gitignored** `keystore.properties` — see
[docs/RELEASE_SIGNING.md](docs/RELEASE_SIGNING.md). Without it the release build
still succeeds but is unsigned.

## Test & verify

```bash
./gradlew :app:testDebugUnitTest      # unit + property-based tests
./gradlew :app:lintDebug              # Android lint
./gradlew dependencyCheckAnalyze      # OWASP dependency vulnerability scan
```

## Releasing

Follow [RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md) before uploading to Play, and
[PLAY_STORE_COMPLIANCE.md](PLAY_STORE_COMPLIANCE.md) for the Data Safety form.

## Documentation

| Doc | What it covers |
|-----|----------------|
| [ARCHITECTURE.md](ARCHITECTURE.md) | layers, patterns, storage |
| [PRIVACY.md](PRIVACY.md) | privacy policy (host this for the Play listing) |
| [PLAY_STORE_COMPLIANCE.md](PLAY_STORE_COMPLIANCE.md) | Data Safety, permissions, ratings |
| [RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md) | pre-upload gate |
| [docs/RELEASE_SIGNING.md](docs/RELEASE_SIGNING.md) | keystore + signed AAB |
| [docs/CICD_SETUP.md](docs/CICD_SETUP.md) | CI, signed release builds, Pages hosting |
| [CHANGELOG.md](CHANGELOG.md) | release history |
