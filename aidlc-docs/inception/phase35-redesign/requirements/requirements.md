# Requirements — Phase 3.5: v2 Visual Redesign

## Intent Analysis
- **Request**: Reskin the entire app to the v2 design
  (`Tessera Photo Puzzle v2.dc.html`) — warm/playful language replacing v1's
  blueprint look.
- **Type**: Enhancement (visual redesign). No new gameplay/data/permissions.
- **Scope**: Cross-cutting UI (theme + all screens + a new drawer interaction).
- **Complexity**: Moderate–High (touches every screen; new theme system).
- **Depth**: Standard–Comprehensive.

## Clarifying Answers (all recommended, "A")
- **Q1=A** Replace the Settings screen with a **left settings drawer** (same
  controls: theme, sound/haptics placeholders, reset stats).
- **Q2=A** **Per-level color palette** — distinct accent per difficulty.
- **Q3=A** Reskin the **dark theme to v2** (keep System/Light/Dark resolution).
- **Q4=A** Add tasteful **v2 motion** (bob/pulse/rise), all reduced-motion gated.
- **Q5=A** **Bundle Nunito** .ttf offline (replaces Barlow).
- **Q6=A** **Remove** now-unused v1 theme pieces (Barlow fonts, blueprint
  registration-mark primitives).

## v2 Design Tokens (authoritative)
**Light**
- Canvas: `#FFE9DA` (app bg), surfaces `#FFF6EF` / `#FFF1E6` / `#F6EAE2`.
- Primary: coral `#F2603C`; primary-light `#FF9E5E`; warm `#FF8A2B`; deep `#D64A28`.
- Ink: `#2E1F1A`; muted `#7A5C50`; faint `#A08076`.
- Accents (per-level + chips): teal `#17B892` / `#0E9A78`, pink `#E0447E`,
  purple `#7C5CFF` / `#B39BFF`, gold `#FFC46B` / `#FFE3BE`.
**Dark**
- Bg `#2A1210` / `#1C1418`; surface `#2A1F24`; text `#FFF1E6`; muted `#C9AFA2` /
  `#A08D84`; accents coral `#FF9E5E`, teal `#4FDDB6`, pink `#E0447E`.
**Shape / elevation / type**
- Radii: pills `999`/`99`, cards `20`/`22`/`24`, large `44`.
- Soft colored shadows (e.g. `0 12px 26px rgba(242,96,60,.4)`).
- Font: **Nunito** (400/600/700/800/900).
- Motion: `bob`, `pulse`, `rise` (spin for spinners).

### Per-level palette (Q2=A)
| Difficulty | Accent (light) |
|---|---|
| Easy | teal `#17B892` |
| Medium | coral `#F2603C` |
| Hard | purple `#7C5CFF` |
(Applied to level cards, board chrome accents, progress. Never color-only for
state — keep labels/meters; A11y preserved.)

## Functional Requirements

### FR35-1 Theme system (v2)
- Replace the theme tokens with the v2 light + dark palettes and Nunito type.
- Rounded shapes + soft shadows as reusable primitives (pill button, rounded
  card, chip, hero header).
- Keep Phase-3 theme resolution (System/Light/Dark) + reduced-motion.

### FR35-2 Settings drawer (Q1=A)
- A **left slide-out drawer** opened from Home (menu/gear affordance), replacing
  the standalone Settings route. Contains: Theme (System/Light/Dark), Sound /
  Haptics placeholder toggles, Reset statistics (confirm). Closes on scrim tap /
  back.

### FR35-3 Full screen reskin
- Restyle every screen to v2: Splash, Home, Difficulty, Puzzle select, Board +
  Pause + Complete, Create flow (Chooser, Camera, Review, Pick size, Generating,
  Permission), My puzzles (library) + delete, empty/error states.
- Cards/chips/buttons use the new rounded/shadowed primitives; Nunito type.

### FR35-4 Per-level color (Q2=A)
- Each difficulty carries its accent through its cards, board accent, and
  progress/meters.

### FR35-5 Motion (Q4=A)
- Splash icon bob; card rise-in on list entry; pulse on loading/generating; all
  **gated by reduced-motion** (Phase-3 `rememberReducedMotion`).

### FR35-6 Fonts (Q5=A)
- Bundle Nunito weights in `res/font`; retire Barlow families.

### FR35-7 Cleanup (Q6=A)
- Remove unused v1 theme pieces: Barlow `.ttf`, blueprint registration-mark
  primitives (`RegistrationFrame` corner marks, square-frame styling) once
  screens no longer use them.

## Non-Functional Requirements
- **NFR35-1 Accessibility preserved** — all Phase-3 a11y (content descriptions,
  ≥48dp targets, tile semantics, non-color-only status, font-scaling-safe) must
  survive the reskin; verify AA contrast on the new palettes (both themes).
- **NFR35-2 Consistency** — colors/shapes/type come from the theme (semantic
  tokens), not per-screen literals; single source of truth (extends the Phase-3
  `LocalTesseraColors` approach with v2 values + new tokens: radii, shadows,
  per-level accent).
- **NFR35-3 No regressions** — architecture, gameplay (adjacent-swap),
  persistence, and existing tests unchanged; app still builds; lint clean.
- **NFR35-4 Offline / permissions** — unchanged (no network, CAMERA-only).
- **NFR35-5 Testing** — reuse existing engine/persistence/image PBT + unit tests
  (unaffected). Add a small pure test if a per-level-accent mapping function is
  extracted. Manual: light/dark v2 look sweep, drawer, tablet, TalkBack.

## Out of Scope
- Audio/haptics behavior (Phase 4), release (Phase 5), docs/compliance (Phase 6).
  No gameplay/data changes.

## Key Requirements Summary
Full **v2 reskin**: Nunito + warm coral/cream palette + colorful per-level
accents, rounded pill/card shapes with soft shadows, tasteful reduced-motion-gated
animation, a **left settings drawer** (replacing the Settings screen), and a v2
dark theme — applied to **every screen** via semantic theme tokens. Architecture,
adjacent-swap gameplay, and persistence are untouched; Phase-3 accessibility and
theme resolution are preserved; unused v1 blueprint assets removed.
