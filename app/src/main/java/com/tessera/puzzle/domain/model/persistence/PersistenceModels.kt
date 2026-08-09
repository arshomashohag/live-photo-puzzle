package com.tessera.puzzle.domain.model.persistence

import com.tessera.puzzle.domain.model.Difficulty

/**
 * Source of a puzzle: bundled with the app or created by the user.
 */
enum class PuzzleSource { BUNDLED, CUSTOM }

/**
 * Theme preference (consumed in a later phase; stored now).
 */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * How a puzzle's image is located. Bundled puzzles reference a drawable by
 * resource name (stable across R-class regeneration); custom puzzles reference
 * app-internal files.
 */
sealed interface ImageRef {
    data class DrawableRef(val resName: String) : ImageRef
    data class FileRef(val imagePath: String, val thumbPath: String) : ImageRef
}

/**
 * A playable puzzle — bundled or custom — represented uniformly.
 */
data class PuzzleRecord(
    val id: String,
    val name: String,
    val source: PuzzleSource,
    val imageRef: ImageRef,
    val createdAt: Long,
    val deletable: Boolean,
)

/**
 * A persisted in-progress game, keyed by (puzzleId, difficulty).
 */
data class SavedBoard(
    val puzzleId: String,
    val difficulty: Difficulty,
    val order: IntArray,
    val selected: Int?,
    val moves: Int,
    val elapsedMillis: Long,
    val updatedAt: Long,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SavedBoard) return false
        return puzzleId == other.puzzleId &&
            difficulty == other.difficulty &&
            order.contentEquals(other.order) &&
            selected == other.selected &&
            moves == other.moves &&
            elapsedMillis == other.elapsedMillis &&
            updatedAt == other.updatedAt
    }

    override fun hashCode(): Int {
        var result = puzzleId.hashCode()
        result = 31 * result + difficulty.hashCode()
        result = 31 * result + order.contentHashCode()
        result = 31 * result + (selected ?: 0)
        result = 31 * result + moves
        result = 31 * result + elapsedMillis.hashCode()
        result = 31 * result + updatedAt.hashCode()
        return result
    }
}

/**
 * Best result for a (puzzleId, difficulty). Ranking metric is lowest time.
 */
data class BestScore(
    val puzzleId: String,
    val difficulty: Difficulty,
    val bestTimeMillis: Long,
    val bestMoves: Int,
    val solvedCount: Int,
    val updatedAt: Long,
)

/**
 * User settings (persisted via DataStore).
 */
data class Settings(
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val theme: ThemeMode = ThemeMode.SYSTEM,
)

/**
 * Derived Home stats (computed, not stored).
 */
data class HomeStats(
    val solvedTotal: Int = 0,
    val bestEasyTimeMillis: Long? = null,
    val createdCount: Int = 0,
)
