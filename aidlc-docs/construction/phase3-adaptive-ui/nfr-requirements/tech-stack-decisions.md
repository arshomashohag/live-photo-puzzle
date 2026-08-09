# Tech-Stack Decisions — Phase 3 Adaptive UI

Additive to the existing Compose/Material3 stack. Pinned in the version catalog.

| Concern | Choice | Version | Rationale |
|---|---|---|---|
| Adaptive size classes | `androidx.compose.material3:material3-window-size-class` | via Compose BOM | Official WindowSizeClass; single-codebase adaptive layouts. |
| Reduced-motion detection | Platform `Settings.Global.ANIMATOR_DURATION_SCALE` | platform | No dep; read animation scale. |
| Compose UI testing | `androidx.compose.ui:ui-test-junit4` (androidTest), `ui-test-manifest` (debug) | via Compose BOM | Light/dark render + semantics assertions. |
| Dark theme | Material3 `darkColorScheme` + `TesseraColors` dark variants | existing | No new dep. |
| PBT | Kotest (present) | 5.9.1 | ThemeResolver/layoutSpec properties. |

## Notes
- No new runtime permissions; no network; still no `INTERNET`.
- material3-window-size-class is part of the Compose Material3 line (BOM-aligned).
