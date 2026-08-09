package com.tessera.puzzle.domain.model

enum class Difficulty(val gridSize: Int) {
    EASY(3),
    MEDIUM(4),
    HARD(5);

    val tileCount: Int get() = gridSize * gridSize
    val label: String get() = name.lowercase().replaceFirstChar { it.uppercase() }
    val level: Int get() = ordinal + 1
}
