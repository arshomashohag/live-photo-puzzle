package com.tessera.puzzle.data.repository

import com.tessera.puzzle.data.db.dao.BoardDao
import com.tessera.puzzle.data.mapper.EntityMappers.toDomainOrNull
import com.tessera.puzzle.data.mapper.EntityMappers.toEntity
import com.tessera.puzzle.di.IoDispatcher
import com.tessera.puzzle.domain.model.Difficulty
import com.tessera.puzzle.domain.model.persistence.SavedBoard
import com.tessera.puzzle.domain.repository.BoardRepository
import com.tessera.puzzle.domain.validation.BoardValidator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BoardRepositoryImpl @Inject constructor(
    private val boardDao: BoardDao,
    @IoDispatcher private val io: CoroutineDispatcher,
) : BoardRepository {

    /**
     * Loads a saved board, validating it (BR-8). If the row is malformed
     * (bad difficulty/CSV or an invalid permutation), it is discarded and null
     * is returned so the caller degrades gracefully instead of crashing.
     */
    override suspend fun loadBoard(
        puzzleId: String,
        difficulty: Difficulty,
    ): SavedBoard? = withContext(io) {
        val entity = boardDao.get(puzzleId, difficulty.name) ?: return@withContext null
        val board = entity.toDomainOrNull()
        val valid = board != null &&
            BoardValidator.isValidOrder(board.order, difficulty.tileCount) &&
            BoardValidator.isValidSelection(board.selected, difficulty.tileCount)
        if (!valid) {
            boardDao.delete(puzzleId, difficulty.name)
            return@withContext null
        }
        board
    }

    override suspend fun saveBoard(board: SavedBoard) = withContext(io) {
        boardDao.upsert(board.toEntity())
    }

    override suspend fun clearBoard(puzzleId: String, difficulty: Difficulty) =
        withContext(io) { boardDao.delete(puzzleId, difficulty.name) }

    override fun observeMostRecent(): Flow<SavedBoard?> =
        boardDao.observeMostRecent().map { entity ->
            entity?.toDomainOrNull()?.takeIf {
                BoardValidator.isValidOrder(it.order, it.difficulty.tileCount)
            }
        }

    override suspend fun boardsForPuzzle(puzzleId: String): List<SavedBoard> =
        withContext(io) {
            boardDao.forPuzzle(puzzleId).mapNotNull { it.toDomainOrNull() }
        }
}
