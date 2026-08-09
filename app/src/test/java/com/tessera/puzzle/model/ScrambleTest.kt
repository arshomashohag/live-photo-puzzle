package com.tessera.puzzle.model

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScrambleTest {

    @Test
    fun scramble_isValidPermutation() {
        val n = 16
        val result = scramble(n, Random(1))
        assertEquals(n, result.size)
        assertEquals((0 until n).toList(), result.sorted())
    }

    @Test
    fun scramble_isNotIdentity() {
        repeat(50) { seed ->
            val n = 9
            val result = scramble(n, Random(seed.toLong()))
            val differs = (0 until n).any { result[it] != it }
            assertTrue("scramble must differ from solved", differs)
        }
    }

    @Test
    fun scramble_isDeterministicForSeed() {
        val a = scramble(25, Random(42))
        val b = scramble(25, Random(42))
        assertTrue(a.contentEquals(b))
    }

    @Test
    fun scramble_smallestBoardNeverIdentity() {
        assertFalse(scramble(9, Random(0)).contentEquals(IntArray(9) { it }))
    }
}
