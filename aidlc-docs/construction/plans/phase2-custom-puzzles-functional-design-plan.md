# Functional Design Plan — Phase 2: Custom Photo Puzzles

**Unit**: `phase2-custom-puzzles`

## Plan Checklist
- [x] Define image-processing pipeline (sample-size math, center-crop, thumbnail)
- [x] Define create flow state machine (chooser→capture/pick→review→size→generate→save→play)
- [x] Define permission state machine (granted/denied/permanently-denied/no-camera)
- [x] Define custom-puzzle naming + save (PuzzleRecord/FileRef reuse)
- [x] Define My-puzzles library + delete-with-confirm behaviors
- [x] Identify PBT-01 testable properties (image math — pure functions)
- [x] Collect answers; resolve ambiguities
- [x] Generate business-logic-model.md, business-rules.md, domain-entities.md
- [x] Security/Resiliency/PBT compliance summary

---

## Clarifying Questions

Answer each after the `[Answer]:` tag. Most behavior is fixed by requirements;
these are the remaining functional details.

## Question 1: Thumbnail size for the library grid
Each saved puzzle stores a small thumbnail for the My-puzzles grid.

A) 256 px square thumbnail (crisp on the grid, tiny file) — recommended

B) 512 px square thumbnail (sharper, slightly larger)

X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 2: What happens to the full processed image after slicing
The board is played from sliced tiles. The full ~1024px image is also saved.
Why keep the full image?

A) Keep the full image (needed to re-slice when replaying at a different size, and for a "preview solved" reference) — recommended

B) Keep only tiles for the created size + thumbnail (smaller storage, but can't replay at other sizes without re-import)

X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 3: Capture failure / very small image handling
If a captured or picked image is too small to make a decent 5×5 (e.g. < ~300px),
or decoding fails:

A) Reject with a friendly message ("This photo is too small / couldn't be read — try another") and return to the chooser — recommended

B) Accept anyway and upscale (may look blurry)

X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 4: Generating screen behavior
The "Generating" step processes + slices the image. On a fast device this is
near-instant.

A) Always show the Generating screen briefly with the blueprint animation (min ~500ms) so the transition reads intentionally, then go to Board — recommended

B) Skip the Generating screen when processing is fast (< ~300ms); show it only if slow

X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 5: My-puzzles ordering
The library grid orders saved puzzles by…

A) Newest first (most recently created at top) — recommended

B) Oldest first

C) Alphabetical by name

X) Other (please describe after [Answer]: tag below)

[Answer]: A
