package com.tessera.puzzle.data

import com.tessera.puzzle.data.mapper.EntityMappers.toDomainOrNull
import com.tessera.puzzle.data.mapper.EntityMappers.toEntity
import com.tessera.puzzle.domain.model.Difficulty
import com.tessera.puzzle.domain.model.persistence.SavedBoard
import com.tessera.puzzle.domain.model.scramble
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll
import kotlin.random.Random

/**
 * Property-based tests for entity↔domain mapping (PBT-02 round-trip, PBT-03
 * invariant). Pure functions, JVM-only.
 */
class MapperPropertiesTest : StringSpec({

    val difficulties = Arb.element(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD)

    "SavedBoard survives entity round-trip (round-trip)" {
        checkAll(Arb.long(), difficulties, Arb.long(0, 10_000_000)) { seed, diff, elapsed ->
            val order = scramble(diff.tileCount, Random(seed))
            val selected = if (seed % 2 == 0L) null else (seed.toInt().mod(diff.tileCount))
            val board = SavedBoard(
                puzzleId = "puzzle-$seed",
                difficulty = diff,
                order = order,
                selected = selected,
                moves = (seed and 0xFF).toInt(),
                elapsedMillis = elapsed,
                updatedAt = elapsed + 1,
            )
            val round = board.toEntity().toDomainOrNull()
            round shouldBe board
        }
    }

    "malformed order CSV maps to null (fail-safe)" {
        val entity = SavedBoard(
            puzzleId = "p",
            difficulty = Difficulty.EASY,
            order = intArrayOf(0, 1, 2),
            selected = null,
            moves = 0,
            elapsedMillis = 0,
            updatedAt = 0,
        ).toEntity().copy(orderCsv = "0,x,2")
        entity.toDomainOrNull() shouldBe null
    }

    "unknown difficulty maps to null (fail-safe)" {
        val entity = SavedBoard(
            puzzleId = "p",
            difficulty = Difficulty.EASY,
            order = intArrayOf(0, 1, 2),
            selected = null,
            moves = 0,
            elapsedMillis = 0,
            updatedAt = 0,
        ).toEntity().copy(difficulty = "IMPOSSIBLE")
        entity.toDomainOrNull() shouldBe null
    }
})
