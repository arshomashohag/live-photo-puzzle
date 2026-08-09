package com.tessera.puzzle.data

import com.tessera.puzzle.R
import com.tessera.puzzle.domain.model.Difficulty
import com.tessera.puzzle.domain.model.Puzzle

object PuzzleCatalog {

    private val easy = listOf(
        Puzzle("easy-1", "Ridgeline", R.drawable.tessera_easy_1),
        Puzzle("easy-2", "Harbour", R.drawable.tessera_easy_2),
        Puzzle("easy-3", "Terrace", R.drawable.tessera_easy_3),
    )
    private val medium = listOf(
        Puzzle("medium-1", "Meridian", R.drawable.tessera_medium_1),
        Puzzle("medium-2", "Lattice", R.drawable.tessera_medium_2),
        Puzzle("medium-3", "Quartz", R.drawable.tessera_medium_3),
    )
    private val hard = listOf(
        Puzzle("hard-1", "Cordon", R.drawable.tessera_hard_1),
        Puzzle("hard-2", "Bastion", R.drawable.tessera_hard_2),
        Puzzle("hard-3", "Cascade", R.drawable.tessera_hard_3),
    )

    val all: List<Puzzle> = easy + medium + hard

    fun forDifficulty(d: Difficulty): List<Puzzle> = when (d) {
        Difficulty.EASY -> easy
        Difficulty.MEDIUM -> medium
        Difficulty.HARD -> hard
    }

    fun byId(id: String): Puzzle? = all.firstOrNull { it.id == id }
}
