package com.tessera.puzzle.domain

import com.tessera.puzzle.domain.model.GuideDecider
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.checkAll

/**
 * Property-based tests for the pure first-run guide decision core. The
 * swipe-guide overlay appears only when it has never been shown AND a board
 * is active; once shown, it stays hidden regardless of board state.
 */
class GuideDeciderPropertiesTest : StringSpec({

    "guide shows iff not-yet-shown and a board is active" {
        checkAll(Arb.boolean(), Arb.boolean()) { shown, hasBoard ->
            GuideDecider.shouldShow(guideShown = shown, hasActiveBoard = hasBoard) shouldBe
                (!shown && hasBoard)
        }
    }

    "once the guide has been shown it never shows again" {
        checkAll(Arb.boolean()) { hasBoard ->
            GuideDecider.shouldShow(guideShown = true, hasActiveBoard = hasBoard) shouldBe false
        }
    }

    "the guide never shows without an active board" {
        checkAll(Arb.boolean()) { shown ->
            GuideDecider.shouldShow(guideShown = shown, hasActiveBoard = false) shouldBe false
        }
    }
})
