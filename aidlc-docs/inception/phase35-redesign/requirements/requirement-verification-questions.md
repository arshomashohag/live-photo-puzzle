# Requirements Clarification Questions — Phase 3.5: v2 Redesign

Answer each after the `[Answer]:` tag; choose "Other" and describe if none fit.

**Established (not re-asked):** Full reskin of all screens to the v2 design
(`Tessera Photo Puzzle v2.dc.html`) — Nunito font; warm cream/peach canvas +
coral-orange primary + colorful accents (pink/teal/purple/gold); rounded
pills/cards + soft colored shadows; bob/pulse/rise motion; left settings drawer;
v2 dark theme. Architecture, gameplay (adjacent-swap), and persistence are
UNCHANGED. Difficulties remain Easy/Medium/Hard. Extension config
(Security/Resiliency/PBT) carries over.

## Question 1: Settings drawer vs the current Settings screen
v2 shows a **left drawer** for settings. We currently have a full Settings
screen (built in Phase 3). How to reconcile?

A) Replace the Settings screen with the v2 left drawer (slide-out from Home), keeping the same controls (theme, sound/haptics placeholders, reset stats) — matches v2 — recommended

B) Keep the Settings as a full screen but restyle it to v2 (no drawer); Home gear opens it

C) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 2: The colorful per-level palette
v2 gives each difficulty its own color (a "colourful level palette").

A) Map Easy/Medium/Hard to distinct v2 accent colors (e.g. Easy=teal/green, Medium=coral/orange, Hard=pink/purple) used on their cards/board chrome — recommended

B) Keep one accent (coral) for all difficulties; just restyle shapes/typography

C) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 3: Dark theme
v2 includes its own dark theme (plum/dark-brown backgrounds, cream text, teal).
Phase 3 already wired theme resolution (System/Light/Dark).

A) Reskin the dark theme to the v2 dark palette (keep the existing System/Light/Dark resolution + Settings control) — recommended

B) Ship light-only for the redesign; revisit dark later

C) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 4: Animations / motion
v2 uses playful motion (bob, pulse, rise) plus the existing sliding-swap.

A) Add tasteful v2 motion (icon bob on splash/idle, rise-in on cards, pulse on loading) — all still gated by the reduced-motion setting from Phase 3 — recommended

B) Minimal motion — just the new look, skip decorative animations

C) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 5: Fonts
v2 uses Nunito (Google Fonts, weights 400–900).

A) Bundle Nunito .ttf files in res/font (offline, no runtime download) — replaces Barlow — recommended

B) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 6: The old blueprint assets
v1's Barlow fonts and blueprint primitives (registration marks, square frames)
become unused after the reskin.

A) Remove the now-unused v1 theme pieces (Barlow fonts, blueprint registration-mark primitives) to keep the codebase clean — recommended

B) Keep them in the repo (dead) for now

C) Other (please describe after [Answer]: tag below)

[Answer]: A
