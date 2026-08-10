# Implementation Summary — Phase 4: Audio & Haptics

Wired the previously-inert Sound/Haptics settings to real feedback. Pure decision
core + thin platform players, app-scoped via Hilt.

## Created
- `domain/model/feedback/FeedbackModels.kt` — `FeedbackEvent`, `HapticKind`,
  `SoundKind`, `FeedbackCue`, and the **pure** `FeedbackDecider.decide(...)`.
- `feedback/SoundPlayer.kt` — SoundPool (USAGE_GAME), lazy-load both SFX, play,
  release; load/play failures are silent no-ops.
- `feedback/HapticPlayer.kt` — Vibrator/VibratorManager (SDK-safe), TICK
  (predefined EFFECT_TICK / one-shot fallback) + SUCCESS waveform; no-vibrator
  no-op.
- `feedback/FeedbackController.kt` — @Singleton; holds live sound/haptics flags
  from SettingsRepository; `onMove()`/`onComplete()` → decide → dispatch.
- `di/FeedbackModule.kt` — `@ApplicationScope` CoroutineScope provider.
- `res/raw/sfx_move.wav` (~60 ms soft click), `res/raw/sfx_complete.wav`
  (ascending 3-note chime) — synthesized (script in scratchpad/gen_sfx.py).
- `test/.../FeedbackDeciderPropertiesTest.kt` — Kotest PBT (5 properties).

## Modified
- `GameViewModel` — inject `FeedbackController`; `onMove()` on a real swap in
  `tap` (moves changed) and `swipe`; `onComplete()` in `onSolved`. No-op/edge
  swipes fire nothing (early return).
- `SettingsDrawerContent` — replaced "Coming soon" notes with real descriptions.
- `AndroidManifest.xml` — added `android.permission.VIBRATE` (install-time).

## Behavior
- Valid move → move sound + light tick (each gated by its own setting).
- Complete → chime + success haptic.
- Edge/no-op swipe → nothing.
- Toggling Sound/Haptics takes effect on the next event.
- No vibrator / SFX load failure → silent, never crashes.
- Reduced-motion does not affect haptics (own toggle governs).

## Requirement mapping
FR4-1→decider+players+GameViewModel · FR4-2→VM early return · FR4-3→onSolved ·
FR4-4→FeedbackController flags · FR4-5→SoundPool volume + hasVibrator no-op ·
FR4-6→drawer copy · NFR4-1→SoundPool · NFR4-2→@Singleton + release · NFR4-6→PBT.

## Verification
`testDebugUnitTest` 45/45 pass (5 new); `lintDebug` 0 errors (caught the missing
VIBRATE permission — fixed); `assembleDebug` OK (Hilt graph resolves).

## Known limitations
- SoundPool/Vibrator are device-verified (manual/instrumented), not JVM — only
  the pure decider is unit/PBT-tested. Please confirm sound + vibration on-device
  and that the Settings toggles mute each channel.
- SFX are synthesized placeholders — swap the two res/raw WAVs anytime.
- `SoundPlayer.release()` exists but the app-singleton lives for the process
  lifetime; the OS reclaims the pool on teardown.
