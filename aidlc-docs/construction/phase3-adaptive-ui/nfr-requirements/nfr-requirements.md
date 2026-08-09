# NFR Requirements — Phase 3 Adaptive UI

## Accessibility (core — NFR3-1)
- **A-1**: Every interactive element exposes a content description / semantic
  label (icon buttons: gear, back, shutter, delete, etc.).
- **A-2**: Board tiles expose position + state semantics.
- **A-3**: Logical focus order; no TalkBack traps.
- **A-4**: All touch targets ≥ 48 dp.
- **A-5**: Text/element contrast meets WCAG AA on **both** themes.
- **A-6**: Layouts font-scaling safe — no clipped text at the largest font scale;
  fixed clipping heights replaced with min-height / wrap.
- **A-7**: Status never color-only.

## Performance (NFR3-2)
- **P-1**: Theme change recolors via `MaterialTheme` without full-tree thrash;
  theme state is a single hoisted StateFlow.
- **P-2**: Reduced-motion honored (animations gated by animator scale).
- **P-3**: Adaptive layout uses `WindowSizeClass` (no manual dp branching per
  screen dimension).

## Security / Privacy / Offline
- UI phase: **SECURITY-09** generic errors preserved. No new permissions, no
  network, no logging of user data. Other security & resiliency rules **N/A**.

## Testability (NFR3-4)
- **T-1 (PBT)**: `ThemeResolver.isDark` truth-table/totality; `layoutSpec`
  monotonic — Kotest.
- **T-2 (Compose UI)**: key screens render in light and dark; Settings theme
  toggle switches theme; a couple of semantics/contentDescription assertions.
- **T-3 (manual matrix)**: small/standard/large phone, tablet, font scales
  (max), light/dark, reduced-motion, TalkBack sweep of the core flow.

## Verification Gates (Build and Test)
- Build + unit/PBT green; lint 0 errors.
- No hardcoded light-only colors remain (grep/token audit).
- Compose UI light/dark checks pass (where run without device: compile; on device:
  execute).
- Manual a11y/tablet/reduced-motion sweep documented.
