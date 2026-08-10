package com.tessera.puzzle.domain

import com.tessera.puzzle.domain.model.BoardState
import com.tessera.puzzle.domain.model.Difficulty
import com.tessera.puzzle.domain.model.Direction
import com.tessera.puzzle.domain.model.Grid
import com.tessera.puzzle.domain.model.Puzzle
import com.tessera.puzzle.domain.model.scramble
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll
import kotlin.random.Random

/**
 * Property-based tests for swipe-to-swap. The swipe interaction must resolve to
 * the exact same board a two-tap adjacent swap would produce, so all engine
 * rules (adjacency, move counting, solvability) are preserved; a swipe toward a
 * board edge (no neighbor) must be a no-op.
 */
class SwipePropertiesTest : StringSpec({

    val puzzle = Puzzle("p", "Test", 0)
    val sizes = listOf(3 to Difficulty.EASY, 4 to Difficulty.MEDIUM, 5 to Difficulty.HARD)

    "swipe toward an existing neighbor == the equivalent two-tap swap" {
        checkAll(Arb.long(), Arb.int(0, 24)) { seed, rawPos ->
            for ((gridSize, diff) in sizes) {
                val n = gridSize * gridSize
                val pos = rawPos % n
                val order = scramble(n, Random(seed))
                for (dir in Direction.entries) {
                    val target = Grid.neighborInDirection(pos, dir, gridSize)
                    if (target != null) {
                        val viaSwipe = board(puzzle, order, diff).swipe(pos, dir)
                        val viaTaps = board(puzzle, order, diff).tapTile(pos).tapTile(target)
                        viaSwipe.order.toList() shouldBe viaTaps.order.toList()
                        viaSwipe.moves shouldBe viaTaps.moves
                        viaSwipe.selected.shouldBeNull()
                    }
                }
            }
        }
    }

    "swipe toward a board edge (no neighbor) is a no-op — order and moves unchanged" {
        checkAll(Arb.long(), Arb.int(0, 24)) { seed, rawPos ->
            for ((gridSize, diff) in sizes) {
                val n = gridSize * gridSize
                val pos = rawPos % n
                val order = scramble(n, Random(seed))
                for (dir in Direction.entries) {
                    if (Grid.neighborInDirection(pos, dir, gridSize) == null) {
                        val before = board(puzzle, order, diff)
                        val after = before.swipe(pos, dir)
                        after.order.toList() shouldBe order.toList()
                        after.moves shouldBe before.moves
                    }
                }
            }
        }
    }

    "swipe increments the move count by exactly one for a valid swap" {
        checkAll(Arb.long(), Arb.int(0, 24)) { seed, rawPos ->
            val gridSize = 4
            val n = gridSize * gridSize
            val pos = rawPos % n
            val order = scramble(n, Random(seed))
            val dir = Direction.entries[seed.toInt().mod(4)]
            val before = board(puzzle, order, Difficulty.MEDIUM)
            val after = before.swipe(pos, dir)
            if (Grid.neighborInDirection(pos, dir, gridSize) != null) {
                after.moves shouldBe before.moves + 1
                after shouldNotBe before
            }
        }
    }
})

private fun board(
    puzzle: Puzzle,
    order: IntArray,
    difficulty: Difficulty,
) = BoardState(
    puzzle = puzzle,
    difficulty = difficulty,
    order = order,
    selected = null,
    moves = 0,
    elapsedMillis = 0L,
)
