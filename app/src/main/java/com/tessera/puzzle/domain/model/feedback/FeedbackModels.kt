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
 * A selectable move sound. The player picks one in Settings; the [SoundPlayer]
 * maps each variant to its bundled raw resource. [label] is the drawer title,
 * [description] the one-line character note. Order here is the drawer order.
 */
enum class MoveSound(val label: String, val description: String) {
    SOFT_TICK("Soft tick", "Muted woody tap"),
    POP("Pop", "Bubbly upward blip"),
    CLICK("Click", "Dry mechanical click"),
    MARIMBA("Marimba", "Warm musical note"),
    GLASS("Glass", "Bright glassy ping"),
}

/**
 * A selectable completion sound. See [MoveSound]; this plays once on solve.
 */
enum class CompleteSound(val label: String, val description: String) {
    ARPEGGIO("Arpeggio", "Rising major run"),
    SPARKLE("Sparkle", "Bright shimmer"),
    CHIME("Chime", "Warm bell"),
    FANFARE("Fanfare", "Playful stab"),
}

/**
 * The concrete sound clip to play, naming the user-selected variant for the
 * event. The [SoundPlayer] maps this to a bundled raw resource.
 */
sealed interface SoundClip {
    data class Move(val variant: MoveSound) : SoundClip
    data class Complete(val variant: CompleteSound) : SoundClip
}

/**
 * The feedback to produce for an event given the current settings. Either
 * channel may be null (disabled by its setting). [sound] names the selected
 * variant to play. Pure value — no Android types.
 */
data class FeedbackCue(
    val sound: SoundClip?,
    val haptic: HapticKind?,
)

/**
 * Pure decision core: maps an event + the user's sound/haptics flags and the
 * selected sound variants to a [FeedbackCue]. Each channel is gated
 * independently so the two settings never interfere. Deterministic and total.
 */
object FeedbackDecider {

    fun decide(
        event: FeedbackEvent,
        soundEnabled: Boolean,
        hapticsEnabled: Boolean,
        moveSound: MoveSound,
        completeSound: CompleteSound,
    ): FeedbackCue {
        val sound: SoundClip? = when (event) {
            FeedbackEvent.MOVE -> SoundClip.Move(moveSound)
            FeedbackEvent.COMPLETE -> SoundClip.Complete(completeSound)
        }.takeIf { soundEnabled }
        val haptic = when (event) {
            FeedbackEvent.MOVE -> HapticKind.TICK
            FeedbackEvent.COMPLETE -> HapticKind.SUCCESS
        }.takeIf { hapticsEnabled }
        return FeedbackCue(sound, haptic)
    }
}
