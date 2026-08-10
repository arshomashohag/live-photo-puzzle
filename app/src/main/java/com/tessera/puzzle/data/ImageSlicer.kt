package com.tessera.puzzle.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlin.math.max
import kotlin.math.min

object ImageSlicer {

    /** Target decoded edge (px) for the board — bounds memory for large files. */
    private const val TARGET_EDGE_PX = 1024

    /** Slice a bundled drawable into [gridSize]² tiles. */
    fun slice(context: Context, @DrawableRes imageRes: Int, gridSize: Int): List<ImageBitmap> {
        val full = BitmapFactory.decodeResource(context.resources, imageRes)
            ?: return emptyList()
        return sliceBitmap(full, gridSize)
    }

    /**
     * Slice a saved image file into [gridSize]² tiles. Bounded decode
     * (inSampleSize) keeps memory in check for high-resolution images. Import
     * already made the file an upright, center-cropped square, so slicing is
     * a straight grid split. Returns an empty list on any decode failure so the
     * caller can surface a recoverable error instead of crashing.
     */
    fun slice(imagePath: String, gridSize: Int): List<ImageBitmap> {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(imagePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return emptyList()
        val opts = BitmapFactory.Options().apply {
            inSampleSize = computeInSampleSize(bounds.outWidth, bounds.outHeight)
        }
        val full = BitmapFactory.decodeFile(imagePath, opts) ?: return emptyList()
        return sliceBitmap(full, gridSize)
    }

    private fun sliceBitmap(full: Bitmap, gridSize: Int): List<ImageBitmap> {
        val side = min(full.width, full.height)
        if (side < gridSize) return emptyList()
        val left = (full.width - side) / 2
        val top = (full.height - side) / 2
        val square = Bitmap.createBitmap(full, left, top, side, side)
        val cell = side / gridSize

        val tiles = ArrayList<ImageBitmap>(gridSize * gridSize)
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                val tile = Bitmap.createBitmap(square, c * cell, r * cell, cell, cell)
                tiles.add(tile.asImageBitmap())
            }
        }
        return tiles
    }

    private fun computeInSampleSize(width: Int, height: Int): Int {
        var sample = 1
        val longest = max(width, height)
        while (longest / (sample * 2) >= TARGET_EDGE_PX) {
            sample *= 2
        }
        return sample
    }
}
