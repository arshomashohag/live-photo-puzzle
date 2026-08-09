package com.tessera.puzzle.domain.model

/**
 * Auto-names custom puzzles: "My Puzzle N" where N = existing count + 1.
 */
object CustomPuzzleNamer {
    fun nextName(existingCustomCount: Int): String =
        "My Puzzle ${existingCustomCount + 1}"
}
