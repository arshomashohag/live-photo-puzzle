package com.tessera.puzzle.domain

import com.tessera.puzzle.domain.model.PuzzleNameInput
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Property-based tests for the pure puzzle-name normalizer. A user-entered
 * name is trimmed, capped at [PuzzleNameInput.MAX_LEN], and falls back to the
 * supplied auto-name when it is blank after trimming.
 */
class PuzzleNameInputPropertiesTest : StringSpec({

    "a blank or whitespace-only entry falls back to the auto-name" {
        forAll(row(""), row(" "), row("   "), row("\t"), row(" \n ")) { blank ->
            PuzzleNameInput.normalize(blank, fallback = "My Puzzle 3") shouldBe "My Puzzle 3"
        }
    }

    "leading and trailing whitespace is trimmed" {
        PuzzleNameInput.normalize("  Beach Day  ", fallback = "X") shouldBe "Beach Day"
    }

    "a non-blank entry is never longer than the cap" {
        checkAll(Arb.string(1, 120)) { raw ->
            val result = PuzzleNameInput.normalize(raw, fallback = "Fallback")
            result.length shouldBeLessThanOrEqual PuzzleNameInput.MAX_LEN
        }
    }

    "a name at or under the cap is preserved verbatim after trimming" {
        val name = "Grandpa's 80th"
        PuzzleNameInput.normalize(name, fallback = "X") shouldBe name
    }

    "an over-long name is truncated to the cap and re-trimmed" {
        val long = "A".repeat(PuzzleNameInput.MAX_LEN + 20)
        PuzzleNameInput.normalize(long, fallback = "X") shouldBe "A".repeat(PuzzleNameInput.MAX_LEN)
    }

    "the result is never blank" {
        checkAll(Arb.string(0, 60)) { raw ->
            PuzzleNameInput.normalize(raw, fallback = "Fallback").isNotBlank() shouldBe true
        }
    }
})
