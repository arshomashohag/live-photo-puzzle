package com.tessera.puzzle.domain

import com.tessera.puzzle.domain.model.ThemeResolver
import com.tessera.puzzle.domain.model.WindowSize
import com.tessera.puzzle.domain.model.layoutSpec
import com.tessera.puzzle.domain.model.persistence.ThemeMode
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.checkAll

/**
 * Property/example tests for pure theme + layout logic.
 */
class ThemeLayoutPropertiesTest : StringSpec({

    "LIGHT is never dark; DARK is always dark (invariant)" {
        checkAll(Arb.boolean()) { systemDark ->
            ThemeResolver.isDark(ThemeMode.LIGHT, systemDark).shouldBeFalse()
            ThemeResolver.isDark(ThemeMode.DARK, systemDark).shouldBeTrue()
        }
    }

    "SYSTEM follows the OS (invariant)" {
        checkAll(Arb.boolean()) { systemDark ->
            ThemeResolver.isDark(ThemeMode.SYSTEM, systemDark) shouldBe systemDark
        }
    }

    "isDark is total over all modes and system states" {
        for (mode in ThemeMode.entries) {
            for (sys in listOf(true, false)) {
                // Must not throw and must return a boolean.
                ThemeResolver.isDark(mode, sys)
            }
        }
    }

    "layoutSpec: columns non-decreasing with size; board cap constant" {
        val compact = layoutSpec(WindowSize.COMPACT)
        val medium = layoutSpec(WindowSize.MEDIUM)
        val expanded = layoutSpec(WindowSize.EXPANDED)
        expanded.gridColumns shouldBeGreaterThanOrEqual medium.gridColumns
        medium.gridColumns shouldBeGreaterThanOrEqual compact.gridColumns
        compact.boardMaxDp shouldBe 560
        medium.boardMaxDp shouldBe 560
        expanded.boardMaxDp shouldBe 560
    }
})
