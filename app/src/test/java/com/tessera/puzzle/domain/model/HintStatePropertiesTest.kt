package com.tessera.puzzle.domain.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

/**
 * Property tests for the pure hint-count core. Proves the count never goes
 * negative, decrements exactly when usable, and resets to MAX.
 */
class HintStatePropertiesTest : StringSpec({

    "fresh starts at MAX" {
        HintState.fresh().remaining shouldBe HintState.MAX
    }

    "use never drops below zero and canUse tracks remaining" {
        checkAll(Arb.int(0, 100)) { n ->
            val s = HintState(n)
            s.canUse shouldBe (n > 0)
            val next = s.use()
            (next.remaining >= 0) shouldBe true
        }
    }

    "use decrements by one when usable, else is identity" {
        checkAll(Arb.int(0, 100)) { n ->
            val s = HintState(n)
            val next = s.use()
            if (n > 0) next.remaining shouldBe n - 1
            else next.remaining shouldBe n
        }
    }

    "using MAX times reaches zero, further use stays zero" {
        var s = HintState.fresh()
        repeat(HintState.MAX) { s = s.use() }
        s.remaining shouldBe 0
        s.use().remaining shouldBe 0
    }
})
