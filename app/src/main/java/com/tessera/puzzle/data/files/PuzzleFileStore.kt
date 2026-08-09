package com.tessera.puzzle.data.files

import android.content.Context
import android.util.Log
import com.tessera.puzzle.domain.model.persistence.ImageRef
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

/**
 * Manages app-internal image files for custom puzzles under
 * filesDir/puzzles/. Fully exercised in Phase 2; defined now so the schema and
 * delete-cleanup path are stable. Never logs file contents (SECURITY-03).
 */
class PuzzleFileStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dir: File by lazy {
        File(context.filesDir, "puzzles").apply { if (!exists()) mkdirs() }
    }

    fun imageFile(id: String): File = File(dir, "$id.jpg")
    fun thumbFile(id: String): File = File(dir, "${id}_thumb.jpg")

    /**
     * Deletes any files referenced by [imageRef]. Best-effort and fail-safe:
     * a missing file or delete failure is logged (no content) and swallowed so
     * the DB row removal still proceeds (SECURITY-15 resource cleanup).
     */
    fun deleteFiles(imageRef: ImageRef) {
        if (imageRef !is ImageRef.FileRef) return
        listOf(imageRef.imagePath, imageRef.thumbPath)
            .filter { it.isNotEmpty() }
            .forEach { path ->
                runCatching { File(path).takeIf { it.exists() }?.delete() }
                    .onFailure { Log.w(TAG, "Failed to delete a puzzle file") }
            }
    }

    /**
     * True iff every file the [imageRef] points to exists. Drawable refs always
     * "exist" (bundled). Used to detect missing custom images (BR-8).
     */
    fun filesExist(imageRef: ImageRef): Boolean = when (imageRef) {
        is ImageRef.DrawableRef -> true
        is ImageRef.FileRef ->
            File(imageRef.imagePath).exists() &&
                (imageRef.thumbPath.isEmpty() || File(imageRef.thumbPath).exists())
    }

    private companion object {
        const val TAG = "PuzzleFileStore"
    }
}
