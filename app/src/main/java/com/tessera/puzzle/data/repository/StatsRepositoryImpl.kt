package com.tessera.puzzle.data.repository

import com.tessera.puzzle.data.db.dao.StatsDao
import com.tessera.puzzle.data.db.dao.PuzzleDao
import com.tessera.puzzle.data.mapper.EntityMappers.toDomainOrNull
import com.tessera.puzzle.data.mapper.EntityMappers.toEntity
import com.tessera.puzzle.di.IoDispatcher
import com.tessera.puzzle.domain.model.Difficulty
import com.tessera.puzzle.domain.model.persistence.BestScore
import com.tessera.puzzle.domain.model.persistence.HomeStats
import com.tessera.puzzle.domain.repository.StatsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StatsRepositoryImpl @Inject constructor(
    private val statsDao: StatsDao,
    private val puzzleDao: PuzzleDao,
    @IoDispatcher private val io: CoroutineDispatcher,
) : StatsRepository {

    /**
     * BR-4/BR-5: increment solvedCount; update best only when the new time is
     * strictly lower (ties keep the earliest best and its move count).
     */
    override suspend fun recordCompletion(
        puzzleId: String,
        difficulty: Difficulty,
        elapsedMillis: Long,
        moves: Int,
    ) = withContext(io) {
        val now = System.currentTimeMillis()
        val existing = statsDao.get(puzzleId, difficulty.name)?.toDomainOrNull()
        val next = if (existing == null) {
            BestScore(puzzleId, difficulty, elapsedMillis, moves, 1, now)
        } else {
            val improved = elapsedMillis < existing.bestTimeMillis
            existing.copy(
                bestTimeMillis = if (improved) elapsedMillis else existing.bestTimeMillis,
                bestMoves = if (improved) moves else existing.bestMoves,
                solvedCount = existing.solvedCount + 1,
                updatedAt = now,
            )
        }
        statsDao.upsert(next.toEntity())
    }

    override suspend fun bestScore(
        puzzleId: String,
        difficulty: Difficulty,
    ): BestScore? = withContext(io) {
        statsDao.get(puzzleId, difficulty.name)?.toDomainOrNull()
    }

    override suspend fun resetAll() = withContext(io) {
        statsDao.deleteAll()
    }

    override fun observeHomeStats(): Flow<HomeStats> = combine(
        statsDao.observeSolvedTotal(),
        statsDao.observeBestEasyTime(),
        puzzleDao.observeCustomCount(),
    ) { solved, bestEasy, created ->
        HomeStats(
            solvedTotal = solved,
            bestEasyTimeMillis = bestEasy,
            createdCount = created,
        )
    }
}
