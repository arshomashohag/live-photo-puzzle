# Tech-Stack Decisions — Phase 3.5 v2 Redesign

Additive/replacement to the existing Compose/Material3 stack.

| Concern | Choice | Rationale |
|---|---|---|
| Font | **Nunito** `.ttf` bundled in `res/font` (400/600/700/800/900) | v2 typography; offline, no runtime download. Replaces Barlow. |
| Shapes/elevation | Compose `RoundedCornerShape` + `Modifier.shadow` / drawing | No new lib; v2 pills/cards/soft shadows. |
| Drawer | Material3 `ModalNavigationDrawer` (present via material3) | Standard left drawer; no new dep. |
| Adaptive/motion | Phase-3 `material3-window-size-class`, `rememberReducedMotion` | Reused. |
| PBT | Kotest (present) | LevelPalette properties. |

## Notes
- No new dependencies beyond Nunito font assets. No network/permissions change.
- Barlow font files removed after screens migrate (Q6=A).
