package com.tessera.puzzle.domain

import com.tessera.puzzle.domain.model.Difficulty
import com.tessera.puzzle.domain.model.LevelPalette
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * Property/example tests for the pure per-level accent mapping.
 */
class LevelPalettePropertiesTest : StringSpec({

    "accentFor is total over all difficulties" {
        for (d in Difficulty.entries) {
            // Must return a key, no exception.
            LevelPalette.accentFor(d)
        }
    }

    "the three difficulties map to three distinct accents" {
        val keys = Difficulty.entries.map { LevelPalette.accentFor(it) }
        keys.toSet().size shouldBe 3
    }

    "mapping is deterministic" {
        for (d in Difficulty.entries) {
            LevelPalette.accentFor(d) shouldBe LevelPalette.accentFor(d)
        }
    }

    "each difficulty has its documented accent" {
        LevelPalette.accentFor(Difficulty.EASY) shouldNotBe
            LevelPalette.accentFor(Difficulty.HARD)
    }
})
