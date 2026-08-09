package com.tessera.puzzle.domain.model

import kotlin.random.Random

class BoardState(
    val puzzle: Puzzle,
    val difficulty: Difficulty,
    val order: IntArray,
    val selected: Int?,
    val moves: Int,
    val elapsedMillis: Long,
) {
    val placedCount: Int
        get() = order.indices.count { order[it] == it }

    val isSolved: Boolean
        get() = order.indices.all { order[it] == it }

    /**
     * Tap handling for edge-sharing (adjacent-only) swaps:
     * - Nothing selected -> select [pos].
     * - Tapping the selected tile -> deselect.
     * - Tapping an orthogonal neighbor of the selected tile -> swap them.
     * - Tapping any other (non-adjacent) tile -> move the selection to [pos]
     *   (pick a new anchor) rather than perform an illegal long-range swap.
     */
    fun tapTile(pos: Int): BoardState {
        val current = selected
        return when {
            current == null -> copy(selected = pos)
            current == pos -> copy(selected = null)
            Grid.areAdjacent(current, pos, difficulty.gridSize) -> {
                val next = order.copyOf()
                val tmp = next[current]
                next[current] = next[pos]
                next[pos] = tmp
                copy(order = next, selected = null, moves = moves + 1)
            }
            else -> copy(selected = pos)
        }
    }

    fun withElapsed(ms: Long): BoardState = copy(elapsedMillis = ms)

    private fun copy(
        order: IntArray = this.order,
        selected: Int? = this.selected,
        moves: Int = this.moves,
        elapsedMillis: Long = this.elapsedMillis,
    ) = BoardState(puzzle, difficulty, order, selected, moves, elapsedMillis)

    companion object {
        fun new(
            puzzle: Puzzle,
            difficulty: Difficulty,
            random: Random = Random.Default,
        ): BoardState =
            BoardState(
                puzzle = puzzle,
                difficulty = difficulty,
                order = scramble(difficulty.tileCount, random),
                selected = null,
                moves = 0,
                elapsedMillis = 0L,
            )
    }
}
