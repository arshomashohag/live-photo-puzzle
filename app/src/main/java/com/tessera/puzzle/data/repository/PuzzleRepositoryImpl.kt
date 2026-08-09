package com.tessera.puzzle.data.repository

import com.tessera.puzzle.data.db.BundledPuzzleSeeder
import com.tessera.puzzle.data.db.dao.BoardDao
import com.tessera.puzzle.data.db.dao.PuzzleDao
import com.tessera.puzzle.data.db.dao.StatsDao
import com.tessera.puzzle.data.files.PuzzleFileStore
import com.tessera.puzzle.data.mapper.EntityMappers.toDomain
import com.tessera.puzzle.data.mapper.EntityMappers.toEntity
import com.tessera.puzzle.di.IoDispatcher
import com.tessera.puzzle.domain.model.persistence.PuzzleRecord
import com.tessera.puzzle.domain.repository.PuzzleRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PuzzleRepositoryImpl @Inject constructor(
    private val puzzleDao: PuzzleDao,
    private val boardDao: BoardDao,
    private val statsDao: StatsDao,
    private val seeder: BundledPuzzleSeeder,
    private val fileStore: PuzzleFileStore,
    @IoDispatcher private val io: CoroutineDispatcher,
) : PuzzleRepository {

    override suspend fun ensureSeeded() = withContext(io) {
        seeder.seedIfNeeded()
    }

    override fun observePuzzles(): Flow<List<PuzzleRecord>> =
        puzzleDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getPuzzle(id: String): PuzzleRecord? = withContext(io) {
        puzzleDao.getById(id)?.toDomain()
    }

    override suspend fun addCustomPuzzle(record: PuzzleRecord) = withContext(io) {
        puzzleDao.upsert(record.toEntity())
    }

    override suspend fun deletePuzzle(id: String) = withContext(io) {
        // BR-7: only deletable (custom) rows; clean up child rows + files.
        val existing = puzzleDao.getById(id)?.toDomain() ?: return@withContext
        if (!existing.deletable) return@withContext
        val deleted = puzzleDao.deleteIfDeletable(id)
        if (deleted > 0) {
            boardDao.deleteForPuzzle(id)
            statsDao.deleteForPuzzle(id)
            fileStore.deleteFiles(existing.imageRef)
        }
    }
}
