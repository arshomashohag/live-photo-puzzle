package com.tessera.puzzle.data

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import kotlin.math.max

/**
 * Covers the pure sampling math behind thumbnail downsampling. The decode
 * itself needs a real BitmapFactory, so it is exercised on-device rather than
 * here; the size contract is what guards against unbounded allocations.
 */
class ThumbnailLoaderTest : StringSpec({

    val dim = Arb.int(1, 8000)
    val target = Arb.int(32, 1024)

    fun isPowerOfTwo(n: Int) = n >= 1 && (n and (n - 1)) == 0

    "sample size is always at least 1 and a power of two" {
        checkAll(dim, dim, target) { w, h, t ->
            val s = ThumbnailLoader.computeInSampleSize(w, h, t)
            s shouldBeGreaterThanOrEqual 1
            isPowerOfTwo(s).shouldBeTrue()
        }
    }

    "sampled longest edge never falls below the target" {
        checkAll(dim, dim, target) { w, h, t ->
            val s = ThumbnailLoader.computeInSampleSize(w, h, t)
            val longest = max(w, h)
            if (longest >= t) {
                (longest / s) shouldBeGreaterThanOrEqual t
            }
        }
    }

    "sampling is maximal: doubling it would undershoot the target" {
        checkAll(dim, dim, target) { w, h, t ->
            val s = ThumbnailLoader.computeInSampleSize(w, h, t)
            (max(w, h) / (s * 2) < t).shouldBeTrue()
        }
    }

    "images at or below the target are not downsampled" {
        ThumbnailLoader.computeInSampleSize(256, 256, 256) shouldBe 1
        ThumbnailLoader.computeInSampleSize(100, 100, 256) shouldBe 1
    }

    "oversized images downsample by the expected factor" {
        ThumbnailLoader.computeInSampleSize(512, 512, 256) shouldBe 2
        ThumbnailLoader.computeInSampleSize(1024, 1024, 256) shouldBe 4
        ThumbnailLoader.computeInSampleSize(4096, 4096, 256) shouldBe 16
    }

    "non-positive inputs degrade to no downsampling" {
        ThumbnailLoader.computeInSampleSize(0, 100, 256) shouldBe 1
        ThumbnailLoader.computeInSampleSize(100, 0, 256) shouldBe 1
        ThumbnailLoader.computeInSampleSize(100, 100, 0) shouldBe 1
    }
})
