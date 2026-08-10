# Phase 4 (Audio & Haptics) — Design + Plan (Streamlined)

Brownfield. Build audio + haptics, gate on existing persisted flags, wire to
game events. Decisions: AI-synthesized WAVs; silent no-op; haptics independent of
reduced-motion.

## Architecture

```
GameViewModel (swipe/tap/onSolved)
   └─ FeedbackController (app singleton, Hilt)
        ├─ reads current Settings (sound/haptics flags)
        ├─ FeedbackDecider  ── pure: (event, flags) → FeedbackCue  [PBT]
        ├─ SoundPlayer      ── SoundPool wrapper (res/raw)         [thin]
        └─ HapticPlayer     ── Vibrator/VibrationEffect wrapper    [thin]
```

- **FeedbackEvent** (enum): `MOVE`, `COMPLETE`. (No-op swipes never call the
  controller, so there is no NONE event to emit — the VM only fires on a real
  swap / solve.)
- **FeedbackCue** (pure result): `playSound: RawRes?`, `haptic: HapticKind?`.
  `FeedbackDecider.decide(event, soundOn, hapticsOn)`:
  - MOVE → sound = sfx_move if soundOn else null; haptic = TICK if hapticsOn.
  - COMPLETE → sound = sfx_complete if soundOn; haptic = SUCCESS if hapticsOn.
  Pure, no Android types (RawRes/HapticKind are plain enums/ints). **PBT here.**
- **FeedbackController** (app-scoped): holds latest flags (collects
  `SettingsRepository.settings` in an app-scope coroutine), exposes
  `onMove()` / `onComplete()`; looks up the cue and dispatches to players.
  Released via `SoundPlayer.release()` on process teardown (best-effort).

## Players
- **SoundPlayer** (SoundPool, `USAGE_GAME`, maxStreams 4): lazy-load both raw
  SFX; `play(rawRes)`; respects media volume implicitly. `release()` frees pool.
  Load failures → silent no-op (Resiliency: never crash).
- **HapticPlayer**: get `Vibrator` (minSdk 29: `getSystemService(VIBRATOR_SERVICE)`;
  API 31+: `VibratorManager`). `TICK` → `VibrationEffect.createPredefined(
  EFFECT_TICK)` (API 29+) else fallback short `createOneShot`. `SUCCESS` →
  a short waveform pattern. `if (!vibrator.hasVibrator()) return` (no-op).

## Assets
- `res/raw/sfx_move.wav` — very short soft click (~60 ms).
- `res/raw/sfx_complete.wav` — short pleasant chime (~500 ms).
- Generated programmatically (Python: sine/decay → 16-bit PCM WAV), committed as
  small binaries. Swappable later.

## Wiring
- `GameViewModel`: inject `FeedbackController`. In `swipe`/`tap`, on a real swap
  → `feedback.onMove()`; in `onSolved` → `feedback.onComplete()`. No-op swipe
  path already returns early → no feedback (FR4-2).
- `SettingsDrawerContent`: drop the "Coming soon" note on Sound/Haptics rows.

## Plan (checkboxes)

### A. Pure decision core + PBT
- [ ] A1 `feedback/FeedbackModels.kt`: `FeedbackEvent`, `HapticKind`,
  `FeedbackCue`; `FeedbackDecider.decide(event, soundOn, hapticsOn)` (pure).
- [ ] A2 `test/.../FeedbackDeciderPropertiesTest.kt` (Kotest): flags gate each
  channel independently; COMPLETE≠MOVE cue; determinism/totality.

### B. Platform players
- [ ] B1 `feedback/SoundPlayer.kt` (SoundPool; lazy load; play; release;
  load-fail no-op).
- [ ] B2 `feedback/HapticPlayer.kt` (Vibrator; TICK/SUCCESS; no-vibrator no-op;
  SDK-safe VibrationEffect).
- [ ] B3 `feedback/FeedbackController.kt` (app singleton; collect settings flags;
  onMove/onComplete → decide → dispatch).
- [ ] B4 `di/FeedbackModule.kt` (@Provides SoundPlayer/HapticPlayer/Controller
  as @Singleton; @ApplicationContext).

### C. Assets
- [ ] C1 Generate `res/raw/sfx_move.wav`, `res/raw/sfx_complete.wav`.

### D. Wiring
- [ ] D1 `GameViewModel`: inject controller; onMove in swipe/tap (real swap);
  onComplete in onSolved.
- [ ] D2 `SettingsDrawerContent`: remove "Coming soon" note.

### E. Tests + build
- [ ] E1 FeedbackDecider PBT green; existing suite unaffected.
- [ ] E2 `assembleDebug` + `testDebugUnitTest` + `lintDebug`.
- [ ] E3 Implementation summary; update aidlc-state.md.

## Traceability
FR4-1→A1,B1,B2,D1 · FR4-2→D1(early-return) · FR4-3→A1,B1,B2,D1 · FR4-4→A1,B3 ·
FR4-5→B1,B2 · FR4-6→D2 · NFR4-1→B1 · NFR4-2→B1,B4 · NFR4-6→A1,A2.

## Known limits
- SoundPool/Vibrator behavior is device-verified (instrumented/manual), not JVM.
  Only the pure decider is unit/PBT-tested.
- Synthesized SFX are placeholders — easily swapped for designed audio later.
