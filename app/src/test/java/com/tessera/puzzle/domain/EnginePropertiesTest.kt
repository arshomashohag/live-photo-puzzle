package com.tessera.puzzle.domain

import com.tessera.puzzle.domain.model.BoardState
import com.tessera.puzzle.domain.model.Difficulty
import com.tessera.puzzle.domain.model.Grid
import com.tessera.puzzle.domain.model.Puzzle
import com.tessera.puzzle.domain.model.scramble
import com.tessera.puzzle.domain.validation.BoardValidator
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeInRange
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll
import kotlin.random.Random

/**
 * Property-based tests (PBT) for the pure engine under the adjacent-only
 * (edge-sharing) swap rule. Complements example-based JUnit4 tests (PBT-10).
 * Kotest provides generation, shrinking, and seeded reproducibility (PBT-07/08).
 */
class EnginePropertiesTest : StringSpec({

    val sizes = listOf(3 to 9, 4 to 16, 5 to 25)
    val puzzle = Puzzle("p", "Test", 0)

    "scramble is a valid permutation (invariant)" {
        checkAll(Arb.long()) { seed ->
            for ((_, n) in sizes) {
                val order = scramble(n, Random(seed))
                order.size shouldBe n
                order.sorted() shouldBe (0 until n).toList()
            }
        }
    }

    "scramble is never the identity (invariant)" {
        checkAll(Arb.long()) { seed ->
            for ((_, n) in sizes) {
                val order = scramble(n, Random(seed))
                (0 until n).any { order[it] != it }.shouldBeTrue()
            }
        }
    }

    "scramble is solvable using only legal adjacent swaps (verification)" {
        // Adjacent transpositions generate the full symmetric group (bubble
        // sort reaches any permutation using only adjacent swaps), so every
        // board is solvable. Verify constructively by row-major bubble sort,
        // where each swap is a legal edge-sharing move via tapTile.
        checkAll(Arb.long()) { seed ->
            for ((gridSize, n) in sizes) {
                var b = board(puzzle, scramble(n, Random(seed)), gridSizeToDiff(gridSize))
                b = bubbleSortAdjacent(b, n)
                b.isSolved.shouldBeTrue()
            }
        }
    }

    "isValidOrder accepts scrambles and rejects malformed (invariant)" {
        checkAll(Arb.long()) { seed ->
            for ((_, n) in sizes) {
                val order = scramble(n, Random(seed))
                BoardValidator.isValidOrder(order, n).shouldBeTrue()
                val broken = order.copyOf().also { it[0] = it[1] }
                BoardValidator.isValidOrder(broken, n).shouldBeFalse()
            }
        }
    }

    "adjacent swap is involutive — swapping the same neighbor pair twice restores order (round-trip)" {
        checkAll(Arb.long(), Arb.int(0, 8)) { seed, rawA ->
            val gridSize = 3
            val n = gridSize * gridSize
            val order = scramble(n, Random(seed))
            val a = rawA % n
            val neighbors = Grid.neighbors(a, gridSize)
            val b = neighbors[(seed.toInt().mod(neighbors.size))]
            val base = board(puzzle, order)
            val once = base.tapTile(a).tapTile(b)
            val twice = once.tapTile(a).tapTile(b)
            twice.order.toList() shouldBe order.toList()
        }
    }

    "non-adjacent tap re-selects and never mutates order (invariant)" {
        checkAll(Arb.long(), Arb.int(0, 8), Arb.int(0, 8)) { seed, rawA, rawB ->
            val gridSize = 3
            val n = gridSize * gridSize
            val a = rawA % n
            val b = rawB % n
            if (a != b && !Grid.areAdjacent(a, b, gridSize)) {
                val order = scramble(n, Random(seed))
                val after = board(puzzle, order).tapTile(a).tapTile(b)
                after.selected shouldBe b
                after.order.toList() shouldBe order.toList()
            }
        }
    }

    "placedCount stays in bounds; equals tileCount iff solved (invariant)" {
        checkAll(Arb.long()) { seed ->
            val n = 9
            val b = board(puzzle, scramble(n, Random(seed)))
            b.placedCount shouldBeInRange 0..n
            (b.placedCount == n) shouldBe b.isSolved
        }
    }
})

private fun gridSizeToDiff(gridSize: Int): Difficulty = when (gridSize) {
    3 -> Difficulty.EASY
    4 -> Difficulty.MEDIUM
    else -> Difficulty.HARD
}

private fun board(
    puzzle: Puzzle,
    order: IntArray,
    difficulty: Difficulty = Difficulty.EASY,
) = BoardState(
    puzzle = puzzle,
    difficulty = difficulty,
    order = order,
    selected = null,
    moves = 0,
    elapsedMillis = 0L,
)

/**
 * Solves a board using only legal edge-sharing swaps, proving solvability.
 *
 * Traverses a "snake" Hamiltonian path (row 0 L→R, row 1 R→L, …) whose
 * consecutive positions are always orthogonal grid neighbors, then bubble-sorts
 * along that path. The target is the solved board (value v at position v), so a
 * value's desired rank along the path is the path-index of the position equal to
 * that value. Adjacent transpositions along a Hamiltonian path generate the full
 * symmetric group, so this reaches solved for any input permutation.
 */
private fun bubbleSortAdjacent(start: BoardState, n: Int): BoardState {
    val gridSize = Math.round(Math.sqrt(n.toDouble())).toInt()
    val path = ArrayList<Int>(n)
    for (row in 0 until gridSize) {
        val cols = if (row % 2 == 0) (0 until gridSize) else (gridSize - 1 downTo 0)
        for (col in cols) path.add(row * gridSize + col)
    }
    // desiredRank[value] = index along the path of the position that value belongs to.
    val desiredRank = IntArray(n)
    for (k in 0 until n) desiredRank[path[k]] = k

    var b = start
    repeat(n) {
        for (k in 0 until n - 1) {
            val posA = path[k]
            val posB = path[k + 1]
            if (desiredRank[b.order[posA]] > desiredRank[b.order[posB]]) {
                b = b.tapTile(posA).tapTile(posB)
            }
        }
    }
    return b
}
