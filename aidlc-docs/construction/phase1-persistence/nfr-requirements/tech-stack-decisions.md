# Tech-Stack Decisions — Phase 1 Persistence

All choices reuse the existing Kotlin/Compose base; additions are additive
Jetpack libraries. Versions are pinned in the Gradle version catalog
(`gradle/libs.versions.toml`) per SECURITY-10 (dependency pinning).

| Concern | Choice | Version (target) | Rationale |
|---|---|---|---|
| Language / UI | Kotlin + Jetpack Compose + Material 3 | existing | Reuse; no change. |
| DI | **Hilt** | 2.52 | User-chosen; standard Android DI; testable wiring. |
| Persistence (metadata) | **Room** | 2.6.1 | Spec-mandated durable storage; parameterized queries (SECURITY-05). |
| Room codegen | **KSP** | matches Kotlin 2.0.20 | Faster than kapt; Room's recommended processor. |
| Settings storage | **DataStore (Preferences)** | 1.1.1 | Async, type-safe prefs; replaces SharedPreferences. |
| State | **StateFlow** (Coroutines) | kotlinx-coroutines 1.8.x (via Compose BOM alignment) | UDF per NFR-1. |
| Property-based testing (PBT-09) | **Kotest** (assertions + property) | 5.9.1 | Kotlin-native PBT; custom generators, shrinking, seed reproducibility (PBT-07/08). |
| Example-based unit tests | **JUnit4** | existing 4.13.2 | Reuse; complements PBT (PBT-10). |
| Room instrumentation tests | Room in-memory DB + AndroidX test | androidx.test/ext-junit | Repository behavior tests. |

## Notes
- **Coroutines dependency**: add `kotlinx-coroutines-core`/`-android` explicitly
  to the catalog (Room/Lifecycle pull it transitively today, but SECURITY-10
  favors explicit, pinned direct deps).
- **Kotest**: `kotest-runner-junit5`, `kotest-property` in `testImplementation`;
  enable JUnit5 platform in the `:app` test options.
- **No new runtime permissions or network deps** — persistence is fully local;
  still no `INTERNET`.
- **KSP plugin** added to the version catalog and applied in `:app`.

## PBT-09 Compliance
- Framework selected (**Kotest Property Testing**) and will be added to project
  dependencies. Supports custom generators, automatic shrinking, and
  seed-based reproducibility. Single language (Kotlin) → one framework. ✅
