package com.tessera.puzzle.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlin.math.min

object ImageSlicer {

    fun slice(context: Context, @DrawableRes imageRes: Int, gridSize: Int): List<ImageBitmap> {
        val full = BitmapFactory.decodeResource(context.resources, imageRes)
            ?: return emptyList()
        val side = min(full.width, full.height)
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
}
