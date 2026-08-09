package com.tessera.puzzle.domain.model

import kotlin.random.Random

fun scramble(tileCount: Int, random: Random = Random.Default): IntArray {
    require(tileCount >= 2) { "tileCount must be >= 2" }
    val order = IntArray(tileCount) { it }
    do {
        for (i in tileCount - 1 downTo 1) {
            val j = random.nextInt(i + 1)
            val tmp = order[i]
            order[i] = order[j]
            order[j] = tmp
        }
    } while (order.withIndex().all { (i, v) -> i == v })
    return order
}
