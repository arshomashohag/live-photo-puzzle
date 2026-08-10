package com.tessera.puzzle.domain.model.feedback

/**
 * A game event that may produce audible/tactile feedback. No-op moves (edge
 * swipes) never reach the feedback layer, so there is no "none" event.
 */
enum class FeedbackEvent { MOVE, COMPLETE }

/** Haptic patterns, resolved to platform effects by the HapticPlayer. */
enum class HapticKind { TICK, SUCCESS }

/** Sound effects, resolved to bundled raw resources by the SoundPlayer. */
enum class SoundKind { MOVE, COMPLETE }

/**
 * The feedback to produce for an event given the current settings. Either
 * channel may be null (disabled by its setting). Pure value — no Android types.
 */
data class FeedbackCue(
    val sound: SoundKind?,
    val haptic: HapticKind?,
)

/**
 * Pure decision core: maps an event + the user's sound/haptics flags to a
 * [FeedbackCue]. Each channel is gated independently so the two settings never
 * interfere. Deterministic and total (defined for every input).
 */
object FeedbackDecider {

    fun decide(
        event: FeedbackEvent,
        soundEnabled: Boolean,
        hapticsEnabled: Boolean,
    ): FeedbackCue {
        val sound = when (event) {
            FeedbackEvent.MOVE -> SoundKind.MOVE
            FeedbackEvent.COMPLETE -> SoundKind.COMPLETE
        }.takeIf { soundEnabled }
        val haptic = when (event) {
            FeedbackEvent.MOVE -> HapticKind.TICK
            FeedbackEvent.COMPLETE -> HapticKind.SUCCESS
        }.takeIf { hapticsEnabled }
        return FeedbackCue(sound, haptic)
    }
}
