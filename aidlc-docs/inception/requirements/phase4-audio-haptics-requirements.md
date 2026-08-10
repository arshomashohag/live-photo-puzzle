# Requirements — Phase 4: Audio & Haptics

**Cycle**: `phase4-audio-haptics` · Brownfield · Depth: Standard.

## Intent
Give the game tactile/audible feedback. The Settings **Sound** and **Haptics**
toggles already persist (DataStore, default on) and flow through
`SettingsViewModel` — but nothing consumes them. Build an audio + haptics layer,
wire it to game events, and gate it on those existing flags.

## Context (verified in code)
- `Settings.soundEnabled` / `hapticsEnabled` persist; `SettingsRepository`
  exposes `setSoundEnabled/setHapticsEnabled`; the drawer switches work.
- No `SoundPool`/`Vibrator` usage anywhere yet; no `res/raw`.
- minSdk 29 → `VibrationEffect` + `VibratorManager` (API 31 has manager; 29/30
  use `getSystemService(Vibrator)`), predefined effects (API 29+) available.
- Game events live in `GameViewModel`: `swipe`/`tap` (move / no-op),
  `onSolved` (completion). Board UI in `BoardScreen`.

## Functional requirements

- **FR4-1 Move feedback**: a valid tile swap (swipe or tap) plays a short move
  sound and a light haptic tick.
- **FR4-2 Invalid/no-op feedback**: a swipe toward a board edge (no neighbor)
  produces NO sound and NO haptic (avoid noise on non-actions). *(Decision: keep
  silent rather than a distinct "blocked" cue — simpler, less annoying.)*
- **FR4-3 Completion feedback**: solving a puzzle plays a distinct celebratory
  sound and a stronger success haptic pattern.
- **FR4-4 Settings gating**: sound only when `soundEnabled`; haptics only when
  `hapticsEnabled`. Toggling takes effect immediately (next event).
- **FR4-5 Respect system**: honor the device ringer/media volume and the
  system "touch sounds"/vibration availability. If the device has no vibrator,
  haptics are a silent no-op (no crash). Sound respects the media stream volume.
- **FR4-6 Drawer copy**: remove the "Coming soon" note from the Sound/Haptics
  rows now that they function.

## Non-functional requirements
- **NFR4-1 Latency**: move feedback must feel immediate (<~30 ms). → `SoundPool`
  (pre-loaded, low-latency) for SFX, not `MediaPlayer`.
- **NFR4-2 Lifecycle/leaks**: release `SoundPool` when no longer needed
  (SECURITY-15 resource cleanup); no leaked `Vibrator`. App-scoped singleton via
  Hilt, released in a controlled way.
- **NFR4-3 Offline/bundled**: SFX bundled in `res/raw` (no network). Small files.
- **NFR4-4 No PII**: nothing logged about photos/user (SECURITY-03) — N/A-ish but
  respected.
- **NFR4-5 Reduced-motion / accessibility**: haptics are independent of visual
  motion; the existing reduced-motion setting does NOT disable haptics (they're
  an accessibility aid). Sound independent too.
- **NFR4-6 Testability (PBT)**: the *decision* logic (should we play move vs
  complete vs nothing, given flags + event) must be pure and property-tested.
  Actual SoundPool/Vibrator are Android-framework (thin, instrumented/manual).

## Asset decision
Bundle 2 short SFX in `res/raw`: `sfx_move` (soft click/tap) and
`sfx_complete` (short chime). **Open item for user**: provide the audio files, or
approve AI generating simple royalty-free/synthesized tones. *(Recommend:
synthesize two tiny WAVs so there is no licensing question and no external
dependency; user can swap later.)*

## Out of scope
- Background music, volume slider in-app, per-event sound customization.
- Distinct "blocked move" cue (FR4-2 keeps it silent).

## Extensions applicability (enabled: Security, Resiliency, PBT — Full)
- **Security**: mostly N/A (no network/PII). SECURITY-15 (resource cleanup for
  SoundPool/Vibrator) enforced. SECURITY-05 (validate any raw resource id).
- **Resiliency**: graceful degradation — missing vibrator / audio-load failure
  must never crash gameplay; feedback silently no-ops.
- **PBT**: Full — pure feedback-decision function property-tested.

## Traceability seed
FR4-1/2/3 → feedback engine + game wiring · FR4-4/6 → settings gating + drawer ·
FR4-5 → platform players · NFR4-1 → SoundPool · NFR4-2 → Hilt singleton + release
· NFR4-6 → pure FeedbackDecider + PBT.
