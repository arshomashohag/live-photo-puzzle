# Requirements Clarification Questions — Phase 3: Adaptive UI, Dark Theme, Accessibility

Answer each after the `[Answer]:` tag; choose "Other" and describe if none fit.

**Already established (not re-asked):** Jetpack Compose + Material 3; the
blueprint design system (Color/Type/Primitives) exists; the source design doc
includes a dark palette and tablet layouts; StateFlow/Hilt architecture; offline;
minSdk 29. Extension config (Security/Resiliency/PBT) carries over.

## Question 1: Dark theme trigger
How should dark mode be chosen?

A) Follow the system setting by default, with a Settings override (System / Light / Dark) — the Settings `theme` field already exists in DataStore — recommended

B) Follow system only (no in-app override this phase)

X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 2: Dynamic color (Material You)
Android 12+ can theme from the user's wallpaper.

A) Do NOT use dynamic color — keep Tessera's steel-blue blueprint identity consistent across devices — recommended

B) Offer dynamic color as an option on Android 12+ (wallpaper-based accents)

X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 3: Tablet / large-screen layout
On tablets and large screens, the phone layout looks sparse.

A) Adaptive layouts via WindowSizeClass: wider content max-width, multi-column grids where sensible, larger board; single codebase (no separate layouts) — recommended

B) Just center the phone layout with max-width on large screens (simpler, less tailored)

X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 4: Board sizing on large screens
The puzzle board is currently full-width square.

A) Cap the board at a comfortable max size and center it (so a 5×5 isn't enormous on a tablet); scale tiles/touch targets accordingly — recommended

B) Keep the board full available width on all screens

X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 5: Reduced-motion / animations
Some users enable "remove animations" in accessibility settings.

A) Respect the system reduced-motion setting: skip/shorten the splash, generating, and any transition animations when it's on — recommended

B) Keep animations regardless

X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 6: Accessibility scope this phase
Which accessibility work is in scope now?

A) Full pass: content descriptions on all interactive elements, board tile semantics (already partial), focus order, ≥48dp targets audit, contrast check on both themes, font-scaling safe layouts, non-color-only status — recommended

B) Content descriptions + font-scaling only; defer focus order / contrast audit

X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 7: Settings screen
A Settings screen is referenced by the design (theme, and later sound/haptics).
Build it now?

A) Build the Settings screen now with the Theme control (System/Light/Dark); leave sound/haptics toggles as placeholders wired to DataStore but noted "Phase 4" — recommended

B) Defer the whole Settings screen to Phase 4 (audio/haptics phase); Phase 3 does theme via system only

X) Other (please describe after [Answer]: tag below)

[Answer]: A
