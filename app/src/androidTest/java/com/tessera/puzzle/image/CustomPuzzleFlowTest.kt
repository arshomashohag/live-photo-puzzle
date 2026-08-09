package com.tessera.puzzle.image

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tessera.puzzle.data.files.PuzzleFileStore
import com.tessera.puzzle.domain.model.persistence.ImageRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

/**
 * Instrumented test for the import → files-written → delete-cleanup path
 * (camera hardware not required; uses a generated file URI).
 */
@RunWith(AndroidJUnit4::class)
class CustomPuzzleFlowTest {

    private val context get() = ApplicationProvider.getApplicationContext<android.content.Context>()

    private fun writeTestImage(edge: Int): Uri {
        val bmp = Bitmap.createBitmap(edge, edge, Bitmap.Config.ARGB_8888)
        Canvas(bmp).apply {
            drawColor(Color.rgb(90, 128, 166))
            val p = android.graphics.Paint().apply { color = Color.WHITE }
            drawRect(0f, 0f, edge / 2f, edge / 2f, p)
        }
        val f = File(context.cacheDir, "test_${edge}.jpg")
        FileOutputStream(f).use { bmp.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        return Uri.fromFile(f)
    }

    @Test
    fun import_writesFiles_andDeleteRemovesThem() = runTest {
        val fileStore = PuzzleFileStore(context)
        val importer = PhotoImporterImpl(context, fileStore, Dispatchers.Unconfined)

        val uri = writeTestImage(1200)
        val result = importer.import(uri, "My Puzzle 1")
        assertTrue("import should succeed", result is ImportResult.Success)

        val record = (result as ImportResult.Success).record
        val ref = record.imageRef as ImageRef.FileRef
        assertTrue("image file exists", File(ref.imagePath).exists())
        assertTrue("thumb file exists", File(ref.thumbPath).exists())
        assertTrue("files reported present", fileStore.filesExist(ref))

        // Delete cleanup.
        fileStore.deleteFiles(ref)
        assertFalse("image file removed", File(ref.imagePath).exists())
        assertFalse("thumb file removed", File(ref.thumbPath).exists())
    }

    @Test
    fun import_rejectsTooSmallImage() = runTest {
        val fileStore = PuzzleFileStore(context)
        val importer = PhotoImporterImpl(context, fileStore, Dispatchers.Unconfined)
        val uri = writeTestImage(200) // below the 300px floor
        assertEquals(ImportResult.TooSmall, importer.import(uri, "x"))
    }
}
