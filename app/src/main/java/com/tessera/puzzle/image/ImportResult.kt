package com.tessera.puzzle.image

import com.tessera.puzzle.domain.model.persistence.PuzzleRecord

/**
 * Result of importing a user photo into a custom puzzle. Failures are values
 * (not exceptions) so the create flow degrades gracefully (RP-1 / SECURITY-15).
 */
sealed interface ImportResult {
    data class Success(val record: PuzzleRecord) : ImportResult
    data object TooSmall : ImportResult
    data object DecodeFailed : ImportResult
    data object IoFailed : ImportResult
}
