package com.tessera.puzzle.domain.repository

import com.tessera.puzzle.domain.model.Difficulty
import com.tessera.puzzle.domain.model.persistence.BestScore
import com.tessera.puzzle.domain.model.persistence.HomeStats
import com.tessera.puzzle.domain.model.persistence.PuzzleRecord
import com.tessera.puzzle.domain.model.persistence.SavedBoard
import com.tessera.puzzle.domain.model.persistence.Settings
import com.tessera.puzzle.domain.model.persistence.ThemeMode
import kotlinx.coroutines.flow.Flow

/**
 * Puzzles — bundled and custom, uniform.
 */
interface PuzzleRepository {
    suspend fun ensureSeeded()
    fun observePuzzles(): Flow<List<PuzzleRecord>>
    suspend fun getPuzzle(id: String): PuzzleRecord?
    suspend fun addCustomPuzzle(record: PuzzleRecord)
    suspend fun deletePuzzle(id: String)
}

/**
 * In-progress boards (one per puzzleId + difficulty).
 */
interface BoardRepository {
    suspend fun loadBoard(puzzleId: String, difficulty: Difficulty): SavedBoard?
    suspend fun saveBoard(board: SavedBoard)
    suspend fun clearBoard(puzzleId: String, difficulty: Difficulty)
    fun observeMostRecent(): Flow<SavedBoard?>
    suspend fun boardsForPuzzle(puzzleId: String): List<SavedBoard>
}

/**
 * Completion stats and best scores.
 */
interface StatsRepository {
    suspend fun recordCompletion(
        puzzleId: String,
        difficulty: Difficulty,
        elapsedMillis: Long,
        moves: Int,
    )

    suspend fun bestScore(puzzleId: String, difficulty: Difficulty): BestScore?
    fun observeHomeStats(): Flow<HomeStats>
    suspend fun resetAll()
}

/**
 * User settings.
 */
interface SettingsRepository {
    val settings: Flow<Settings>
    suspend fun setSoundEnabled(enabled: Boolean)
    suspend fun setHapticsEnabled(enabled: Boolean)
    suspend fun setTheme(mode: ThemeMode)
    suspend fun setGuideShown(shown: Boolean)
}
