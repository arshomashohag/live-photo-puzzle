package com.tessera.puzzle.domain.model

import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Produces a scrambled, solvable board for a square grid of [tileCount] tiles.
 *
 * Because swaps are restricted to edge-sharing (adjacent) tiles, a plain random
 * permutation may be unreachable. Generating the scramble by applying many
 * random ADJACENT swaps from the solved board guarantees the result is always
 * solvable via legal adjacent swaps. The result is never the identity.
 *
 * @param tileCount total tiles; must be a perfect square (9, 16, 25, ...).
 */
fun scramble(tileCount: Int, random: Random = Random.Default): IntArray {
    require(tileCount >= 4) { "tileCount must be >= 4" }
    val gridSize = sqrt(tileCount.toDouble()).roundToInt()
    require(gridSize * gridSize == tileCount) { "tileCount must be a perfect square" }

    val order = IntArray(tileCount) { it }
    // Enough adjacent swaps to fully mix the board (scaled by size).
    val swapCount = tileCount * 8
    var anchor = random.nextInt(tileCount)
    repeat(swapCount) {
        val neighbors = Grid.neighbors(anchor, gridSize)
        val target = neighbors[random.nextInt(neighbors.size)]
        val tmp = order[anchor]
        order[anchor] = order[target]
        order[target] = tmp
        anchor = target
    }
    // Guarantee non-identity: if we happened to land back on solved, nudge once.
    if (order.withIndex().all { (i, v) -> i == v }) {
        val neighbors = Grid.neighbors(0, gridSize)
        val target = neighbors[random.nextInt(neighbors.size)]
        val tmp = order[0]; order[0] = order[target]; order[target] = tmp
    }
    return order
}
