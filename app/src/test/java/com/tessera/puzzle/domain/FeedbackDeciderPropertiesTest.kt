package com.tessera.puzzle.domain

import com.tessera.puzzle.domain.model.feedback.FeedbackDecider
import com.tessera.puzzle.domain.model.feedback.FeedbackEvent
import com.tessera.puzzle.domain.model.feedback.HapticKind
import com.tessera.puzzle.domain.model.feedback.SoundKind
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.enum
import io.kotest.property.checkAll

/**
 * Property-based tests for the pure feedback decision core. The two settings
 * must gate their channels independently, and each event maps to a stable cue.
 */
class FeedbackDeciderPropertiesTest : StringSpec({

    "sound channel present iff soundEnabled (independent of haptics)" {
        checkAll(Arb.enum<FeedbackEvent>(), Arb.boolean(), Arb.boolean()) { ev, s, h ->
            val cue = FeedbackDecider.decide(ev, s, h)
            if (s) (cue.sound != null) shouldBe true else cue.sound.shouldBeNull()
        }
    }

    "haptic channel present iff hapticsEnabled (independent of sound)" {
        checkAll(Arb.enum<FeedbackEvent>(), Arb.boolean(), Arb.boolean()) { ev, s, h ->
            val cue = FeedbackDecider.decide(ev, s, h)
            if (h) (cue.haptic != null) shouldBe true else cue.haptic.shouldBeNull()
        }
    }

    "event maps to the matching kinds when enabled" {
        checkAll(Arb.enum<FeedbackEvent>()) { ev ->
            val cue = FeedbackDecider.decide(ev, soundEnabled = true, hapticsEnabled = true)
            when (ev) {
                FeedbackEvent.MOVE -> {
                    cue.sound shouldBe SoundKind.MOVE
                    cue.haptic shouldBe HapticKind.TICK
                }
                FeedbackEvent.COMPLETE -> {
                    cue.sound shouldBe SoundKind.COMPLETE
                    cue.haptic shouldBe HapticKind.SUCCESS
                }
            }
        }
    }

    "both off yields a fully silent cue" {
        checkAll(Arb.enum<FeedbackEvent>()) { ev ->
            val cue = FeedbackDecider.decide(ev, soundEnabled = false, hapticsEnabled = false)
            cue.sound.shouldBeNull()
            cue.haptic.shouldBeNull()
        }
    }

    "decision is deterministic" {
        checkAll(Arb.enum<FeedbackEvent>(), Arb.boolean(), Arb.boolean()) { ev, s, h ->
            FeedbackDecider.decide(ev, s, h) shouldBe FeedbackDecider.decide(ev, s, h)
        }
    }
})
