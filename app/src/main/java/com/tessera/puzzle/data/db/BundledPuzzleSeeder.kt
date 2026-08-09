package com.tessera.puzzle.data.db

import android.content.Context
import com.tessera.puzzle.data.PuzzleCatalog
import com.tessera.puzzle.data.db.dao.PuzzleDao
import com.tessera.puzzle.data.mapper.EntityMappers.toEntity
import com.tessera.puzzle.domain.model.persistence.ImageRef
import com.tessera.puzzle.domain.model.persistence.PuzzleRecord
import com.tessera.puzzle.domain.model.persistence.PuzzleSource
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Seeds the 9 bundled puzzles as rows on first run (BR-1). Idempotent: only
 * inserts when no BUNDLED rows exist, and uses INSERT-IGNORE so re-runs never
 * duplicate. Bundled images are referenced by drawable resource NAME (stable
 * across R-class regeneration).
 */
class BundledPuzzleSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val puzzleDao: PuzzleDao,
) {
    suspend fun seedIfNeeded() {
        if (puzzleDao.countBySource(PuzzleSource.BUNDLED.name) > 0) return
        val now = System.currentTimeMillis()
        val records = PuzzleCatalog.all.map { p ->
            PuzzleRecord(
                id = p.id,
                name = p.name,
                source = PuzzleSource.BUNDLED,
                imageRef = ImageRef.DrawableRef(
                    context.resources.getResourceEntryName(p.imageRes),
                ),
                createdAt = now,
                deletable = false,
            )
        }
        puzzleDao.insertIgnore(records.map { it.toEntity() })
    }
}
