package com.tessera.puzzle.domain.model

/**
 * Pure normalizer for a user-entered custom-puzzle name. Trims surrounding
 * whitespace, caps the length at [MAX_LEN], and falls back to a supplied
 * auto-name when the entry is blank. The UI still shows the name single-line
 * with an ellipsis; this only bounds what gets stored.
 */
object PuzzleNameInput {
    const val MAX_LEN = 40

    /**
     * @param raw the user's raw text-field input.
     * @param fallback the auto-generated name to use when [raw] is blank.
     * @return a trimmed, length-capped, never-blank name.
     */
    fun normalize(raw: String, fallback: String): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return fallback
        return trimmed.take(MAX_LEN).trim()
    }
}
