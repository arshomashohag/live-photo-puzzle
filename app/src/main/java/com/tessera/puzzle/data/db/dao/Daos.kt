package com.tessera.puzzle.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tessera.puzzle.data.db.entity.BestScoreEntity
import com.tessera.puzzle.data.db.entity.PuzzleEntity
import com.tessera.puzzle.data.db.entity.SavedBoardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PuzzleDao {
    @Query("SELECT * FROM puzzles ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<PuzzleEntity>>

    @Query("SELECT * FROM puzzles WHERE id = :id")
    suspend fun getById(id: String): PuzzleEntity?

    @Query("SELECT COUNT(*) FROM puzzles WHERE source = :source")
    suspend fun countBySource(source: String): Int

    @Query("SELECT COUNT(*) FROM puzzles WHERE source = 'CUSTOM'")
    fun observeCustomCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(puzzles: List<PuzzleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(puzzle: PuzzleEntity)

    @Query("DELETE FROM puzzles WHERE id = :id AND deletable = 1")
    suspend fun deleteIfDeletable(id: String): Int
}

@Dao
interface BoardDao {
    @Query("SELECT * FROM saved_boards WHERE puzzleId = :puzzleId AND difficulty = :difficulty")
    suspend fun get(puzzleId: String, difficulty: String): SavedBoardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(board: SavedBoardEntity)

    @Query("DELETE FROM saved_boards WHERE puzzleId = :puzzleId AND difficulty = :difficulty")
    suspend fun delete(puzzleId: String, difficulty: String)

    @Query("DELETE FROM saved_boards WHERE puzzleId = :puzzleId")
    suspend fun deleteForPuzzle(puzzleId: String)

    @Query("SELECT * FROM saved_boards ORDER BY updatedAt DESC LIMIT 1")
    fun observeMostRecent(): Flow<SavedBoardEntity?>

    @Query("SELECT * FROM saved_boards WHERE puzzleId = :puzzleId")
    suspend fun forPuzzle(puzzleId: String): List<SavedBoardEntity>
}

@Dao
interface StatsDao {
    @Query("SELECT * FROM best_scores WHERE puzzleId = :puzzleId AND difficulty = :difficulty")
    suspend fun get(puzzleId: String, difficulty: String): BestScoreEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(score: BestScoreEntity)

    @Query("DELETE FROM best_scores WHERE puzzleId = :puzzleId")
    suspend fun deleteForPuzzle(puzzleId: String)

    @Query("SELECT COALESCE(SUM(solvedCount), 0) FROM best_scores")
    fun observeSolvedTotal(): Flow<Int>

    @Query("SELECT MIN(bestTimeMillis) FROM best_scores WHERE difficulty = 'EASY'")
    fun observeBestEasyTime(): Flow<Long?>

    @Query("DELETE FROM best_scores")
    suspend fun deleteAll()
}
