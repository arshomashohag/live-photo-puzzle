package com.tessera.puzzle.presentation

import androidx.compose.ui.graphics.ImageBitmap
import com.tessera.puzzle.domain.model.BoardState
import com.tessera.puzzle.domain.model.Difficulty
import com.tessera.puzzle.domain.model.persistence.BestScore
import com.tessera.puzzle.domain.model.persistence.HomeStats
import com.tessera.puzzle.domain.model.persistence.PuzzleRecord

/**
 * A puzzle plus whether it has a saved in-progress board at a difficulty
 * (drives the Puzzle Select resume indicator).
 */
data class PuzzleListItem(
    val puzzle: PuzzleRecord,
    val resumableDifficulties: Set<Difficulty> = emptySet(),
)

/**
 * A compact summary of the most-recently-played in-progress board for the Home
 * Continue card.
 */
data class ContinueInfo(
    val puzzleId: String,
    val puzzleName: String,
    val difficulty: Difficulty,
    val placed: Int,
    val total: Int,
)

data class HomeUiState(
    val stats: HomeStats = HomeStats(),
    val continueInfo: ContinueInfo? = null,
    val restoreNotice: Boolean = false,
)

data class BoardUiState(
    val board: BoardState? = null,
    val tiles: List<ImageBitmap> = emptyList(),
)

data class CompleteUiState(
    val puzzleName: String = "",
    val difficulty: Difficulty = Difficulty.EASY,
    val elapsedMillis: Long = 0,
    val moves: Int = 0,
    val best: BestScore? = null,
)
