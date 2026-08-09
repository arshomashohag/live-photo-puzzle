# NFR Requirements Plan — Phase 3.5 v2 Redesign

**Unit**: `phase35-redesign`

## Plan Checklist
- [x] Category applicability (Accessibility + Performance; Security minimal; Resiliency/Scalability N/A)
- [x] Tech-stack (Nunito fonts; no new libs beyond Phase-3 adaptive)
- [x] Accessibility/contrast NFRs on v2 palettes
- [x] Performance NFRs (recomposition, motion, shadow cost)
- [x] Testability NFRs (LevelPalette PBT; existing suite intact)
- [x] Generate nfr-requirements.md + tech-stack-decisions.md
- [x] Compliance summary

## Category Applicability
| Category | Applicable? | Justification |
|---|---|---|
| Accessibility | **Yes** | Contrast must be re-verified on the warm v2 palettes (both themes). |
| Performance | **Yes (light)** | Soft shadows / motion should not cause jank; scoped recomposition. |
| Security | **Minimal** | Visual phase — SECURITY-09 preserved; rest N/A. |
| Resiliency | **N/A** | No new failure surface. |
| Scalability | **N/A** | Single-user on-device. |

## Open Questions
None — settled by requirements/FD; only Nunito fonts are added.
