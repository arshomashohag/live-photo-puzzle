package com.tessera.puzzle.domain.model

/**
 * Center square-crop rectangle within a source of width×height.
 */
data class CropRect(val left: Int, val top: Int, val size: Int)

/**
 * Pure image-geometry math for the import pipeline. No Android types, so it is
 * JVM-unit- and property-testable.
 */
object ImageMath {

    /**
     * Largest power-of-two sample size such that the decoded image stays at
     * least [targetEdgePx] on both edges (standard BitmapFactory downsample).
     * Returns at least 1.
     */
    fun computeInSampleSize(srcW: Int, srcH: Int, targetEdgePx: Int): Int {
        require(srcW > 0 && srcH > 0 && targetEdgePx > 0) { "dimensions must be positive" }
        var sample = 1
        // Double the sample while both edges would still be >= target.
        while (srcW / (sample * 2) >= targetEdgePx && srcH / (sample * 2) >= targetEdgePx) {
            sample *= 2
        }
        return sample
    }

    /**
     * Center square crop for a [srcW]×[srcH] image. Always within bounds; for a
     * square input it is the whole image (left=top=0, size=edge).
     */
    fun centerCropSquare(srcW: Int, srcH: Int): CropRect {
        require(srcW > 0 && srcH > 0) { "dimensions must be positive" }
        val size = minOf(srcW, srcH)
        val left = (srcW - size) / 2
        val top = (srcH - size) / 2
        return CropRect(left, top, size)
    }

    /**
     * True iff the shorter edge is at least [minEdgePx].
     */
    fun isLargeEnough(srcW: Int, srcH: Int, minEdgePx: Int): Boolean =
        minOf(srcW, srcH) >= minEdgePx
}
