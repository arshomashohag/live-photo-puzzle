# Functional Design Plan — Phase 3: Adaptive UI, Dark Theme, Accessibility

**Unit**: `phase3-adaptive-ui`

## Plan Checklist
- [ ] Define ThemeResolver (pure: ThemeMode + system-dark → effective dark)
- [ ] Define dark color scheme (tokens for both themes)
- [ ] Define adaptive rules (WindowSizeClass → columns/max-width/board cap)
- [ ] Define reduced-motion rule
- [ ] Define Settings screen behavior
- [ ] Define accessibility semantics conventions
- [ ] Identify PBT-01 properties (ThemeResolver)
- [ ] Collect answers; resolve ambiguities
- [ ] Generate business-logic-model.md, business-rules.md, domain-entities.md
- [ ] Compliance summary

---

## Clarifying Questions

Answer each after the `[Answer]:` tag. Most is settled by requirements; these are
the remaining functional details.

## Question 1: Dark palette derivation
The design doc has a dark splash color (#1D2D3D) and steel/sky accents. For the
full dark scheme:

A) Derive a cohesive dark scheme from the existing tokens (dark canvas ~#1D2D3D / #14202B, paper→light-on-dark text, keep steel #5980A6 + sky #94BCE3 accents which already read on dark) — recommended

B) Introduce a distinct dark palette (describe in Other)

X) Other (please describe after [Answer]: tag below)

[Answer]:

## Question 2: Expanded-width column counts
On Expanded (tablet) screens, grids currently 2–3 columns. Target?

A) Difficulty grid stays 3 across (already fits); puzzle-select & my-puzzles go from 2 → 3 columns on Expanded; content max-width ~840dp centered — recommended

B) Keep current column counts, just center with max-width

X) Other (please describe after [Answer]: tag below)

[Answer]:

## Question 3: Board maximum size
Cap the centered board at…

A) 560 dp max (comfortable on tablets; phones unaffected) — recommended

B) 480 dp max

X) Other (please describe after [Answer]: tag below)

[Answer]:

## Question 4: Settings — Reset stats
Include a "Reset statistics" action in Settings this phase?

A) Yes — with a confirmation dialog; clears best scores/solved counts (keeps custom puzzles) — recommended

B) No — defer; Settings has only Theme + placeholder toggles this phase

X) Other (please describe after [Answer]: tag below)

[Answer]:
