package com.tessera.puzzle.domain.model

/**
 * Grid adjacency for a square board. Positions are row-major
 * (index = row * gridSize + col). Two positions are adjacent when they share an
 * edge orthogonally (up/down/left/right) — no diagonals, no wraparound. Corner
 * tiles have 2 neighbors, edge tiles 3, interior tiles 4.
 */
object Grid {

    fun areAdjacent(a: Int, b: Int, gridSize: Int): Boolean {
        if (a == b) return false
        val ra = a / gridSize
        val ca = a % gridSize
        val rb = b / gridSize
        val cb = b % gridSize
        val rowDiff = kotlin.math.abs(ra - rb)
        val colDiff = kotlin.math.abs(ca - cb)
        return (rowDiff == 1 && colDiff == 0) || (rowDiff == 0 && colDiff == 1)
    }

    /**
     * Orthogonal neighbor positions of [pos] on a [gridSize]×[gridSize] board.
     */
    fun neighbors(pos: Int, gridSize: Int): List<Int> {
        val row = pos / gridSize
        val col = pos % gridSize
        val result = ArrayList<Int>(4)
        if (row > 0) result.add(pos - gridSize)
        if (row < gridSize - 1) result.add(pos + gridSize)
        if (col > 0) result.add(pos - 1)
        if (col < gridSize - 1) result.add(pos + 1)
        return result
    }

    /**
     * The neighbor of [pos] in [direction] on a [gridSize]×[gridSize] board, or
     * null when [pos] is on that edge (no neighbor there). Used by swipe-to-swap:
     * a swipe toward a board edge is a no-op.
     */
    fun neighborInDirection(pos: Int, direction: Direction, gridSize: Int): Int? {
        val row = pos / gridSize
        val col = pos % gridSize
        return when (direction) {
            Direction.UP -> if (row > 0) pos - gridSize else null
            Direction.DOWN -> if (row < gridSize - 1) pos + gridSize else null
            Direction.LEFT -> if (col > 0) pos - 1 else null
            Direction.RIGHT -> if (col < gridSize - 1) pos + 1 else null
        }
    }
}

/** Swipe direction on the board. */
enum class Direction { UP, DOWN, LEFT, RIGHT }
