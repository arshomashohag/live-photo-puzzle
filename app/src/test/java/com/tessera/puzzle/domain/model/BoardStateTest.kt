package com.tessera.puzzle.domain.model

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardStateTest {

    private val puzzle = Puzzle("p1", "Ridgeline", 0)

    private fun solvedOrder(n: Int) = IntArray(n) { it }

    private fun board(order: IntArray) = BoardState(
        puzzle = puzzle,
        difficulty = Difficulty.EASY,
        order = order,
        selected = null,
        moves = 0,
        elapsedMillis = 0L,
    )

    @Test
    fun new_isScrambledAndUnsolved() {
        val b = BoardState.new(puzzle, Difficulty.EASY, Random(3))
        assertEquals(9, b.order.size)
        assertFalse(b.isSolved)
        assertNull(b.selected)
        assertEquals(0, b.moves)
    }

    @Test
    fun tapTile_selectsThenSwaps() {
        val start = board(intArrayOf(1, 0, 2, 3, 4, 5, 6, 7, 8))
        val afterFirstTap = start.tapTile(0)
        assertEquals(0, afterFirstTap.selected)
        assertEquals(0, afterFirstTap.moves)

        val afterSwap = afterFirstTap.tapTile(1)
        assertNull(afterSwap.selected)
        assertEquals(1, afterSwap.moves)
        assertTrue(afterSwap.isSolved)
    }

    @Test
    fun tapTile_sameTileDeselects() {
        val start = board(intArrayOf(1, 0, 2, 3, 4, 5, 6, 7, 8))
        val selected = start.tapTile(0)
        val deselected = selected.tapTile(0)
        assertNull(deselected.selected)
        assertEquals(0, deselected.moves)
    }

    @Test
    fun placedCount_countsCorrectTiles() {
        val b = board(intArrayOf(0, 1, 2, 3, 4, 5, 6, 8, 7))
        assertEquals(7, b.placedCount)
        assertFalse(b.isSolved)
    }

    @Test
    fun isSolved_trueForIdentity() {
        assertTrue(board(solvedOrder(9)).isSolved)
        assertEquals(9, board(solvedOrder(9)).placedCount)
    }

    @Test
    fun withElapsed_updatesTimeOnly() {
        val b = board(solvedOrder(9)).withElapsed(1234L)
        assertEquals(1234L, b.elapsedMillis)
    }
}
