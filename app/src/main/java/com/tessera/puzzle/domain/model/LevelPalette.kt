package com.tessera.puzzle.domain.model

/**
 * Per-difficulty accent key. The actual color is resolved from the theme in the
 * UI layer so it flips light/dark.
 */
enum class LevelAccentKey { TEAL, CORAL, PURPLE }

/**
 * Maps a difficulty to its v2 accent (colourful level palette).
 */
object LevelPalette {
    fun accentFor(difficulty: Difficulty): LevelAccentKey = when (difficulty) {
        Difficulty.EASY -> LevelAccentKey.TEAL
        Difficulty.MEDIUM -> LevelAccentKey.CORAL
        Difficulty.HARD -> LevelAccentKey.PURPLE
    }
}
