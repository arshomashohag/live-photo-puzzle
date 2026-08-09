package com.tessera.puzzle.domain

import com.tessera.puzzle.domain.model.CustomPuzzleNamer
import com.tessera.puzzle.domain.model.ImageMath
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

/**
 * Property-based tests for the pure image math + naming (PBT). Complements
 * example-based tests (PBT-10); seeded/shrinking via Kotest (PBT-07/08).
 */
class ImageMathPropertiesTest : StringSpec({

    val dim = Arb.int(1, 8000)
    val target = Arb.int(64, 2048)

    fun isPowerOfTwo(n: Int) = n >= 1 && (n and (n - 1)) == 0

    "computeInSampleSize is >= 1 and a power of two" {
        checkAll(dim, dim, target) { w, h, t ->
            val s = ImageMath.computeInSampleSize(w, h, t)
            s shouldBeGreaterThanOrEqual 1
            isPowerOfTwo(s).shouldBeTrue()
        }
    }

    "decoded edges stay >= target when source is large enough" {
        checkAll(Arb.int(1, 8000), Arb.int(1, 8000), target) { w, h, t ->
            val s = ImageMath.computeInSampleSize(w, h, t)
            // If the source is at least target on an edge, the downsampled edge
            // must remain >= target (the guarantee of the sample-size choice).
            if (w >= t) (w / s) shouldBeGreaterThanOrEqual t
            if (h >= t) (h / s) shouldBeGreaterThanOrEqual t
        }
    }

    "computeInSampleSize is monotonic non-increasing in target" {
        checkAll(Arb.int(1, 8000), Arb.int(1, 8000), Arb.int(64, 1024)) { w, h, t ->
            val small = ImageMath.computeInSampleSize(w, h, t)
            val large = ImageMath.computeInSampleSize(w, h, t * 2)
            large shouldBeLessThanOrEqual small
        }
    }

    "centerCropSquare is within bounds" {
        checkAll(dim, dim) { w, h ->
            val c = ImageMath.centerCropSquare(w, h)
            c.left shouldBeGreaterThanOrEqual 0
            c.top shouldBeGreaterThanOrEqual 0
            c.size shouldBe minOf(w, h)
            (c.left + c.size) shouldBeLessThanOrEqual w
            (c.top + c.size) shouldBeLessThanOrEqual h
        }
    }

    "centerCropSquare of a square is the whole image" {
        checkAll(dim) { e ->
            val c = ImageMath.centerCropSquare(e, e)
            c shouldBe com.tessera.puzzle.domain.model.CropRect(0, 0, e)
        }
    }

    "isLargeEnough matches the shorter-edge threshold" {
        checkAll(dim, dim, Arb.int(1, 4000)) { w, h, min ->
            ImageMath.isLargeEnough(w, h, min) shouldBe (minOf(w, h) >= min)
        }
    }

    "nextName format and distinctness" {
        checkAll(Arb.int(0, 10000)) { n ->
            CustomPuzzleNamer.nextName(n) shouldBe "My Puzzle ${n + 1}"
        }
        CustomPuzzleNamer.nextName(0) shouldBe "My Puzzle 1"
    }
})
