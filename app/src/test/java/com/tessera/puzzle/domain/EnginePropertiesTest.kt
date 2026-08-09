package com.tessera.puzzle.domain

import com.tessera.puzzle.domain.model.BoardState
import com.tessera.puzzle.domain.model.Difficulty
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
 * Property-based tests (PBT) for the pure engine. Complements the example-based
 * JUnit4 tests (PBT-10). Kotest provides generation, shrinking, and seeded
 * reproducibility (PBT-07/08).
 */
class EnginePropertiesTest : StringSpec({

    val tileCounts = listOf(9, 16, 25)
    val puzzle = Puzzle("p", "Test", 0)

    "scramble is a valid permutation (invariant)" {
        checkAll(Arb.long()) { seed ->
            for (n in tileCounts) {
                val order = scramble(n, Random(seed))
                order.size shouldBe n
                order.sorted() shouldBe (0 until n).toList()
            }
        }
    }

    "scramble is never the identity (invariant)" {
        checkAll(Arb.long()) { seed ->
            for (n in tileCounts) {
                val order = scramble(n, Random(seed))
                (0 until n).any { order[it] != it }.shouldBeTrue()
            }
        }
    }

    "isValidOrder accepts scrambles and rejects malformed (invariant)" {
        checkAll(Arb.long()) { seed ->
            for (n in tileCounts) {
                val order = scramble(n, Random(seed))
                BoardValidator.isValidOrder(order, n).shouldBeTrue()
                // Break the permutation: duplicate an index.
                val broken = order.copyOf().also { it[0] = it[1] }
                BoardValidator.isValidOrder(broken, n).shouldBeFalse()
            }
        }
    }

    "swap is involutive — swapping the same pair twice restores order (round-trip)" {
        checkAll(Arb.long(), Arb.int(0, 8), Arb.int(0, 8)) { seed, rawA, rawB ->
            val n = 9
            val order = scramble(n, Random(seed))
            val a = rawA % n
            val b = rawB % n
            if (a != b) {
                val base = board(puzzle, order)
                // Tap a (select), tap b (swap), tap a (select), tap b (swap back).
                val once = base.tapTile(a).tapTile(b)
                val twice = once.tapTile(a).tapTile(b)
                twice.order.toList() shouldBe order.toList()
            }
        }
    }

    "swap is order-independent — A then B equals B then A (commutativity)" {
        checkAll(Arb.long(), Arb.int(0, 8), Arb.int(0, 8)) { seed, rawA, rawB ->
            val n = 9
            val order = scramble(n, Random(seed))
            val a = rawA % n
            val b = rawB % n
            if (a != b) {
                val ab = board(puzzle, order).tapTile(a).tapTile(b)
                val ba = board(puzzle, order).tapTile(b).tapTile(a)
                ab.order.toList() shouldBe ba.order.toList()
            }
        }
    }

    "solved oracle — sorting via swaps reaches solved; placedCount in bounds" {
        checkAll(Arb.long()) { seed ->
            val n = 9
            var b = board(puzzle, scramble(n, Random(seed)))
            b.placedCount shouldBeInRange 0..n
            // Selection-sort the board using swaps: place value i at position i.
            for (i in 0 until n) {
                if (b.order[i] != i) {
                    val j = (i until n).first { b.order[it] == i }
                    b = b.tapTile(i).tapTile(j)
                }
            }
            b.isSolved.shouldBeTrue()
            b.placedCount shouldBe n
        }
    }
})

private fun board(puzzle: Puzzle, order: IntArray) = BoardState(
    puzzle = puzzle,
    difficulty = Difficulty.EASY,
    order = order,
    selected = null,
    moves = 0,
    elapsedMillis = 0L,
)
