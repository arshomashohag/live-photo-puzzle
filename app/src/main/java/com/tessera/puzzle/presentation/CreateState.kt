package com.tessera.puzzle.presentation

import android.net.Uri
import com.tessera.puzzle.domain.model.Difficulty

/**
 * State machine for the custom-puzzle create flow (UDF via StateFlow).
 */
sealed interface CreateState {
    data object Chooser : CreateState
    data object RequestingPermission : CreateState
    data object PermissionDenied : CreateState
    data object Camera : CreateState
    data class Review(val source: Uri) : CreateState
    data class PickSize(val source: Uri) : CreateState
    data class Generating(val source: Uri, val difficulty: Difficulty) : CreateState
    data class Error(val message: String) : CreateState
    data class Ready(val puzzleId: String, val difficulty: Difficulty) : CreateState
}
