package com.tessera.puzzle.presentation

import android.net.Uri
import com.tessera.puzzle.domain.model.Difficulty

/**
 * State machine for the custom-puzzle create flow (UDF via StateFlow).
 */
sealed interface CreateState {
    /**
     * Transient entry state: the host routes it to the camera (requesting
     * permission) on camera-equipped devices, or straight to the gallery picker
     * when the device has no camera. There is no chooser screen.
     */
    data object Launching : CreateState
    data object RequestingPermission : CreateState
    data object PermissionDenied : CreateState
    data object Camera : CreateState

    /** The system photo picker is open; the UI shows a neutral placeholder. */
    data object PickingGallery : CreateState
    data class Review(val source: Uri) : CreateState
    data class NameYourPuzzle(val source: Uri) : CreateState
    data class PickSize(val source: Uri, val name: String) : CreateState
    data class Generating(
        val source: Uri,
        val difficulty: Difficulty,
        val name: String,
    ) : CreateState
    data class Error(val message: String) : CreateState
    data class Ready(val puzzleId: String, val difficulty: Difficulty) : CreateState
}
