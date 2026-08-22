package com.tessera.puzzle.domain

import com.tessera.puzzle.domain.model.feedback.CompleteSound
import com.tessera.puzzle.domain.model.feedback.FeedbackDecider
import com.tessera.puzzle.domain.model.feedback.FeedbackEvent
import com.tessera.puzzle.domain.model.feedback.HapticKind
import com.tessera.puzzle.domain.model.feedback.MoveSound
import com.tessera.puzzle.domain.model.feedback.SoundClip
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.enum
import io.kotest.property.checkAll

/**
 * Property-based tests for the pure feedback decision core. The two settings
 * must gate their channels independently, each event maps to a stable cue, and
 * the sound channel names the user-selected variant for that event.
 */
class FeedbackDeciderPropertiesTest : StringSpec({

    "sound channel present iff soundEnabled (independent of haptics)" {
        checkAll(
            Arb.enum<FeedbackEvent>(), Arb.boolean(), Arb.boolean(),
            Arb.enum<MoveSound>(), Arb.enum<CompleteSound>(),
        ) { ev, s, h, mv, cp ->
            val cue = FeedbackDecider.decide(ev, s, h, mv, cp)
            if (s) (cue.sound != null) shouldBe true else cue.sound.shouldBeNull()
        }
    }

    "haptic channel present iff hapticsEnabled (independent of sound)" {
        checkAll(
            Arb.enum<FeedbackEvent>(), Arb.boolean(), Arb.boolean(),
            Arb.enum<MoveSound>(), Arb.enum<CompleteSound>(),
        ) { ev, s, h, mv, cp ->
            val cue = FeedbackDecider.decide(ev, s, h, mv, cp)
            if (h) (cue.haptic != null) shouldBe true else cue.haptic.shouldBeNull()
        }
    }

    "event maps to the matching haptic when enabled" {
        checkAll(Arb.enum<MoveSound>(), Arb.enum<CompleteSound>()) { mv, cp ->
            FeedbackDecider.decide(FeedbackEvent.MOVE, true, true, mv, cp)
                .haptic shouldBe HapticKind.TICK
            FeedbackDecider.decide(FeedbackEvent.COMPLETE, true, true, mv, cp)
                .haptic shouldBe HapticKind.SUCCESS
        }
    }

    "sound clip names the selected variant for the event" {
        checkAll(Arb.enum<MoveSound>(), Arb.enum<CompleteSound>()) { mv, cp ->
            FeedbackDecider.decide(FeedbackEvent.MOVE, true, true, mv, cp)
                .sound shouldBe SoundClip.Move(mv)
            FeedbackDecider.decide(FeedbackEvent.COMPLETE, true, true, mv, cp)
                .sound shouldBe SoundClip.Complete(cp)
        }
    }

    "both off yields a fully silent cue regardless of selected variants" {
        checkAll(
            Arb.enum<FeedbackEvent>(), Arb.enum<MoveSound>(), Arb.enum<CompleteSound>(),
        ) { ev, mv, cp ->
            val cue = FeedbackDecider.decide(ev, false, false, mv, cp)
            cue.sound.shouldBeNull()
            cue.haptic.shouldBeNull()
        }
    }

    "decision is deterministic" {
        checkAll(
            Arb.enum<FeedbackEvent>(), Arb.boolean(), Arb.boolean(),
            Arb.enum<MoveSound>(), Arb.enum<CompleteSound>(),
        ) { ev, s, h, mv, cp ->
            FeedbackDecider.decide(ev, s, h, mv, cp) shouldBe
                FeedbackDecider.decide(ev, s, h, mv, cp)
        }
    }
})
