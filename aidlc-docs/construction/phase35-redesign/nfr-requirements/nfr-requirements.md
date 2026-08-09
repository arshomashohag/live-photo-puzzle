# NFR Requirements — Phase 3.5 v2 Redesign

## Accessibility (NFR35-1)
- **A-1**: All Phase-3 semantics/labels/≥48dp/tile-semantics/font-scaling
  survive the reskin.
- **A-2**: **WCAG AA contrast** verified for text and key controls on the v2
  **light** palette (ink `#2E1F1A` on cream surfaces; coral on cream; on-primary
  white on coral) and **dark** palette (cream `#FFF1E6` on plum; accents). Adjust
  a token if a pairing fails; document.
- **A-3**: Per-level color is never the sole state signal (labels/meters kept).
- **A-4**: Rounded controls keep ≥48dp touch area regardless of visual pill size.

## Performance
- **P-1**: Soft shadows use Compose `shadow`/elevation efficiently; avoid
  large blur on many list items (cap or use tonal surface where cheaper).
- **P-2**: Motion (bob/pulse/rise) is lightweight and reduced-motion gated; no
  sustained recomposition from decorative animation.
- **P-3**: Theme/token reads via composition local (no per-frame allocation).

## Security / Privacy / Offline
- Visual phase: **SECURITY-09** generic errors preserved. No new permissions,
  network, or logging. Other security & resiliency rules **N/A**.

## Consistency (NFR35-2)
- All color/shape/type from semantic tokens (`TesseraColors`/`TesseraShapes`/
  `TesseraType`), not per-screen literals. Per-level accent via `LevelPalette` +
  `accentColor`.

## No Regressions (NFR35-3)
- Architecture, gameplay, persistence, and existing tests unchanged; build +
  lint stay green.

## Testability (NFR35-5)
- **T-1 (PBT)**: `LevelPalette.accentFor` totality + distinctness (Kotest).
- **T-2**: Existing engine/persistence/image/theme PBT + unit tests still pass.
- **T-3 (manual)**: v2 look sweep light+dark, drawer open/close, per-level colors,
  tablet, largest font scale, TalkBack, contrast spot-checks.

## Verification Gates (Build and Test)
- Build + all unit/PBT green; lint 0 errors.
- No off-theme hardcoded colors remain (audit); Barlow/blueprint removed with no
  dangling refs.
- Contrast spot-checks recorded (manual).
