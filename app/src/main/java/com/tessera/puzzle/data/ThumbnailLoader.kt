package com.tessera.puzzle.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlin.math.max

/**
 * Bounded decoding for grid thumbnails. Card thumbs are shown at roughly one
 * grid cell, so decoding a file at its native size wastes memory proportional
 * to the number of cards on screen. Every load here is a two-pass decode:
 * bounds first, then a sampled decode no smaller than the requested edge.
 */
object ThumbnailLoader {

    /** Edge (px) thumbnails are written at by the import pipeline. */
    const val DEFAULT_EDGE_PX = 256

    /**
     * Decode [path] downsampled to approximately [targetEdgePx] per edge.
     * Returns null if the file is missing, unreadable, or not a valid image,
     * so callers can fall back to a placeholder instead of crashing.
     *
     * Thumbnails are opaque JPEGs, so [Bitmap.Config.RGB_565] is used: half
     * the bytes per pixel of ARGB_8888 with no visible loss for photos and no
     * alpha channel to preserve.
     */
    fun load(path: String, targetEdgePx: Int = DEFAULT_EDGE_PX): ImageBitmap? =
        runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

            val opts = BitmapFactory.Options().apply {
                inSampleSize = computeInSampleSize(
                    bounds.outWidth,
                    bounds.outHeight,
                    targetEdgePx,
                )
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            BitmapFactory.decodeFile(path, opts)?.asImageBitmap()
        }.getOrNull()

    /**
     * Largest power-of-two sample size keeping the longest edge at or above
     * [targetEdgePx]. Mirrors the standard BitmapFactory downsample contract
     * and never returns less than 1.
     */
    fun computeInSampleSize(width: Int, height: Int, targetEdgePx: Int): Int {
        if (width <= 0 || height <= 0 || targetEdgePx <= 0) return 1
        var sample = 1
        val longest = max(width, height)
        while (longest / (sample * 2) >= targetEdgePx) {
            sample *= 2
        }
        return sample
    }
}
