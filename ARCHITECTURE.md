# Architecture — Tessera

Tessera is a single-module Android app (`:app`) written in Kotlin with Jetpack
Compose. It follows a layered, unidirectional-data-flow (UDF) architecture with
**pure decision cores** that hold the game logic in plain, testable functions.

## Layers

```
UI (Compose)  →  ViewModel (state + intents)  →  domain (pure logic)
                                              ↘   data (storage, images)
```

- **UI — `ui/`** : Compose screens and theming. Renders immutable state and
  emits user intents. No business logic.
- **Presentation / game — `game/`, `presentation/`** : ViewModels
  (`GameViewModel`, `CreateViewModel`) expose `StateFlow` state and translate
  intents into domain calls. State flows down, events flow up (UDF).
- **Domain — `domain/`** : the heart of the app. Plain Kotlin, no Android
  dependencies, fully unit-testable.
  - `domain/model/` — value types and **pure decision cores**:
    `HintState` (hint budget), `BoardState`/`Grid`/`Scramble` (puzzle state),
    `ThemeResolver`, `LevelPalette`, `ImageMath`, `CustomPuzzleNamer`,
    `LayoutSpec`, `Difficulty`.
  - `domain/validation/` — `BoardValidator`.
  - `domain/repository/` — repository interfaces (`Repositories.kt`).
- **Data — `data/`** : implements the domain repository interfaces.
  - `data/settings/` — DataStore-backed settings.
  - `data/repository/` — board/stats/puzzle repositories.
  - `data/db/` — Room database, DAOs, entities, bundled-puzzle seeding.
  - `data/files/` — `PuzzleFileStore` for custom-puzzle images on disk.
  - `data/ImageSlicer.kt`, `data/PuzzleCatalog.kt` — image tiling + built-in set.

## Cross-cutting components

- **DI — `di/`** : Hilt modules wire repositories, camera, and platform
  services. `TesseraApplication` is the Hilt entry point.
- **Camera — `camera/`, `image/`** : CameraX capture (`CameraController`) and
  photo import (`PhotoImporter`) for creating custom puzzles.
- **Feedback — `feedback/`** : `FeedbackController` coordinates `SoundPlayer`
  and `HapticPlayer`, gated by user settings.

## Key patterns

- **Pure decision cores + property-based testing.** Game rules live in pure
  functions (e.g. `HintState`, `ImageSlicer.tileBounds`, feedback deciders).
  These are covered by Kotest **property-based** tests (`checkAll`), not just
  example tests — the same pattern used across the project's phases.
- **Unidirectional data flow.** ViewModels own state as `StateFlow`; Compose
  observes it; user actions are dispatched back as intents. No two-way binding.
- **Repository abstraction.** Domain depends on interfaces
  (`domain/repository`), implemented in `data/` — keeping storage details out of
  the game logic.
- **Local-only persistence.** Settings via DataStore; custom puzzles via Room +
  app-private files. Nothing leaves the device (see [PRIVACY.md](PRIVACY.md)).

## Data & control flow: solving a puzzle (example)

1. `GameViewModel` loads a `Puzzle` (built-in or custom) and slices the image
   via `ImageSlicer` into tiles plus a full-image bitmap.
2. User moves tiles → intents update `BoardState`; `BoardValidator` decides
   when the board is solved.
3. On solve, the UI shows a brief full-image reveal, then the results screen.
4. Hints are governed by the pure `HintState` (a fixed budget per game);
   requesting one overlays the full image for a couple of seconds while the
   timer keeps running.

## Build & tech notes

- **Kotlin + Compose + Material 3**, Hilt DI, CameraX, Room, DataStore.
- Gradle toolchain runs on **JDK 21**; app compiles to **Java 17** bytecode.
- `minSdk 29`, `targetSdk / compileSdk 35`.
- See [README.md](README.md) for build/run/test commands.
