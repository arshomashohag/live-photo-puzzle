package com.tessera.puzzle.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import com.tessera.puzzle.data.files.PuzzleFileStore
import com.tessera.puzzle.di.IoDispatcher
import com.tessera.puzzle.domain.model.ImageMath
import com.tessera.puzzle.domain.model.persistence.ImageRef
import com.tessera.puzzle.domain.model.persistence.PuzzleRecord
import com.tessera.puzzle.domain.model.persistence.PuzzleSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

/**
 * Turns a user-supplied image [Uri] into a saved custom puzzle: bounded decode,
 * EXIF-upright, center-crop to square, scale to the target edge, and write the
 * full image + thumbnail to app-internal storage. Memory-safe and fail-safe.
 */
interface PhotoImporter {
    suspend fun import(source: Uri, name: String): ImportResult
}

class PhotoImporterImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileStore: PuzzleFileStore,
    @IoDispatcher private val io: CoroutineDispatcher,
) : PhotoImporter {

    override suspend fun import(source: Uri, name: String): ImportResult =
        withContext(io) {
            val resolver = context.contentResolver
            try {
                // Pass 1: bounds only (no pixel allocation). decodeStream returns
                // null by design when inJustDecodeBounds=true — it only fills
                // outWidth/outHeight — so success is measured by the stream
                // opening and the bounds being populated, NOT the decode result.
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                val boundsStream = resolver.openInputStream(source)
                    ?: return@withContext ImportResult.DecodeFailed
                boundsStream.use { BitmapFactory.decodeStream(it, null, bounds) }

                val srcW = bounds.outWidth
                val srcH = bounds.outHeight
                if (srcW <= 0 || srcH <= 0) return@withContext ImportResult.DecodeFailed
                if (!ImageMath.isLargeEnough(srcW, srcH, MIN_EDGE_PX)) {
                    return@withContext ImportResult.TooSmall
                }

                // Pass 2: bounded decode.
                val opts = BitmapFactory.Options().apply {
                    inSampleSize = ImageMath.computeInSampleSize(srcW, srcH, TARGET_EDGE_PX)
                }
                var decoded = resolver.openInputStream(source)?.use {
                    BitmapFactory.decodeStream(it, null, opts)
                } ?: return@withContext ImportResult.DecodeFailed

                decoded = applyExifRotation(resolver, source, decoded)

                // Center-crop to square.
                val crop = ImageMath.centerCropSquare(decoded.width, decoded.height)
                val square = Bitmap.createBitmap(decoded, crop.left, crop.top, crop.size, crop.size)
                if (square != decoded) decoded.recycle()

                // Scale to the exact target edge for the full image.
                val full = square.scaleToSquare(TARGET_EDGE_PX)
                if (full != square) square.recycle()
                val thumb = full.scaleToSquare(THUMB_EDGE_PX)

                val id = UUID.randomUUID().toString()
                val imageFile = fileStore.imageFile(id)
                val thumbFile = fileStore.thumbFile(id)
                writeJpeg(full, imageFile.absolutePath)
                writeJpeg(thumb, thumbFile.absolutePath)
                full.recycle()
                if (thumb != full) thumb.recycle()

                ImportResult.Success(
                    PuzzleRecord(
                        id = id,
                        name = name,
                        source = PuzzleSource.CUSTOM,
                        imageRef = ImageRef.FileRef(imageFile.absolutePath, thumbFile.absolutePath),
                        createdAt = System.currentTimeMillis(),
                        deletable = true,
                    ),
                )
            } catch (e: OutOfMemoryError) {
                Log.w(TAG, "Import failed: out of memory")
                ImportResult.DecodeFailed
            } catch (e: SecurityException) {
                Log.w(TAG, "Import failed: source not accessible")
                ImportResult.IoFailed
            } catch (e: Exception) {
                Log.w(TAG, "Import failed")
                ImportResult.IoFailed
            }
        }

    private fun applyExifRotation(
        resolver: android.content.ContentResolver,
        source: Uri,
        bitmap: Bitmap,
    ): Bitmap {
        val degrees = runCatching {
            resolver.openInputStream(source)?.use { stream ->
                when (ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL,
                )) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } ?: 0f
        }.getOrDefault(0f)
        if (degrees == 0f) return bitmap
        val m = Matrix().apply { postRotate(degrees) }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
        if (rotated != bitmap) bitmap.recycle()
        return rotated
    }

    private fun Bitmap.scaleToSquare(edge: Int): Bitmap =
        if (width == edge && height == edge) this
        else Bitmap.createScaledBitmap(this, edge, edge, true)

    private fun writeJpeg(bitmap: Bitmap, path: String) {
        FileOutputStream(path).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
        }
    }

    private companion object {
        const val TAG = "PhotoImporter"
        const val TARGET_EDGE_PX = 1024
        const val THUMB_EDGE_PX = 256
        const val MIN_EDGE_PX = 300
        const val JPEG_QUALITY = 85
    }
}
