package com.tessera.puzzle.data

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

/**
 * Property-based tests proving tile slicing is pixel-perfect: the boundaries
 * cover the full edge with no gap, overlap, or dropped pixel, so a solved board
 * reconstructs the image exactly. Grid sizes cover the app's 3/4/5 plus a range.
 */
class TileBoundsPropertiesTest : StringSpec({

    val gridSizes = listOf(3, 4, 5)

    "bounds start at 0 and end exactly at side (no dropped edge pixels)" {
        checkAll(Arb.int(1, 8192)) { side ->
            for (g in gridSizes) {
                if (side >= g) {
                    val b = ImageSlicer.tileBounds(side, g)
                    b.first() shouldBe 0
                    b.last() shouldBe side
                }
            }
        }
    }

    "tiles are contiguous — each starts where the previous ends (no gap/overlap)" {
        checkAll(Arb.int(1, 8192), Arb.int(2, 12)) { side, g ->
            if (side >= g) {
                val b = ImageSlicer.tileBounds(side, g)
                b.size shouldBe g + 1
                for (i in 0 until g) {
                    (b[i + 1] >= b[i]) shouldBe true
                }
            }
        }
    }

    "tile widths sum to side exactly (full reconstruction, no missing stripe)" {
        checkAll(Arb.int(1, 8192), Arb.int(2, 12)) { side, g ->
            if (side >= g) {
                val b = ImageSlicer.tileBounds(side, g)
                val widths = (0 until g).map { b[it + 1] - b[it] }
                widths.sum() shouldBe side
                widths.all { it >= 1 } shouldBe true
            }
        }
    }

    "tile sizes differ by at most one pixel (even distribution of remainder)" {
        checkAll(Arb.int(2, 8192), Arb.int(2, 12)) { side, g ->
            if (side >= g) {
                val b = ImageSlicer.tileBounds(side, g)
                val widths = (0 until g).map { b[it + 1] - b[it] }
                (widths.max() - widths.min() <= 1) shouldBe true
            }
        }
    }
})
