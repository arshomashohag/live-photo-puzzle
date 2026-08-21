package com.tessera.puzzle.presentation

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tessera.puzzle.domain.model.CustomPuzzleNamer
import com.tessera.puzzle.domain.model.Difficulty
import com.tessera.puzzle.domain.model.PuzzleNameInput
import com.tessera.puzzle.domain.model.persistence.PuzzleSource
import com.tessera.puzzle.camera.CameraController
import com.tessera.puzzle.domain.repository.PuzzleRepository
import com.tessera.puzzle.image.ImportResult
import com.tessera.puzzle.image.PhotoImporter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import kotlin.system.measureTimeMillis

/**
 * Drives the create-a-custom-puzzle flow. Reuses PuzzleRepository (save) and
 * PhotoImporter (memory-safe processing). Autosave/board play is handled by the
 * shared GameViewModel after Ready.
 */
@HiltViewModel
class CreateViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val puzzleRepository: PuzzleRepository,
    private val photoImporter: PhotoImporter,
    val cameraController: CameraController,
) : ViewModel() {

    /** True if the device has any camera (drives camera-vs-picker fallback). */
    val hasCamera: Boolean get() = cameraController.hasCamera()

    private val _state = MutableStateFlow<CreateState>(CreateState.Launching)
    val state: StateFlow<CreateState> = _state.asStateFlow()

    /**
     * Resolve the transient [CreateState.Launching] entry state: go to the
     * camera (requesting permission) when the device has a camera, otherwise
     * straight to the gallery picker. Called by the host once.
     */
    fun launchDefault() {
        _state.value =
            if (hasCamera) CreateState.RequestingPermission
            else CreateState.PickingGallery
    }

    /** Open the system photo picker from the camera's gallery shortcut. */
    fun openGallery() { _state.value = CreateState.PickingGallery }

    /** User dismissed the picker without choosing: return to the camera. */
    fun pickCancelled() { _state.value = CreateState.RequestingPermission }

    fun cameraReady() { _state.value = CreateState.Camera }
    fun permissionDenied() { _state.value = CreateState.PermissionDenied }

    fun onCaptured(uri: Uri) { _state.value = CreateState.Review(uri) }
    fun onPicked(uri: Uri) { _state.value = CreateState.Review(uri) }

    /** Retake from Review reopens the live camera. */
    fun retake() { _state.value = CreateState.RequestingPermission }

    fun accept() {
        val s = _state.value
        if (s is CreateState.Review) _state.value = CreateState.NameYourPuzzle(s.source)
    }

    /**
     * Confirm the user-entered [rawName] for the captured photo. Normalized via
     * [PuzzleNameInput] — trimmed, length-capped, and falling back to the next
     * auto-name ("My Puzzle N") when left blank — then advance to size pick.
     */
    fun confirmName(rawName: String) {
        val s = _state.value
        if (s !is CreateState.NameYourPuzzle) return
        viewModelScope.launch {
            val fallback = CustomPuzzleNamer.nextName(customCountSnapshot())
            val name = PuzzleNameInput.normalize(rawName, fallback)
            _state.value = CreateState.PickSize(s.source, name)
        }
    }

    fun pickSize(difficulty: Difficulty) {
        val s = _state.value
        if (s !is CreateState.PickSize) return
        _state.value = CreateState.Generating(s.source, difficulty, s.name)
        generate(s.source, difficulty, s.name)
    }

    private fun generate(source: Uri, difficulty: Difficulty, name: String) {
        viewModelScope.launch {
            var result: ImportResult
            val elapsed = measureTimeMillis {
                result = photoImporter.import(source, name)
            }
            // Minimum-duration transition (BR2-8).
            val remaining = MIN_GENERATING_MS - elapsed
            if (remaining > 0) kotlinx.coroutines.delay(remaining)

            when (val r = result) {
                is ImportResult.Success -> {
                    puzzleRepository.addCustomPuzzle(r.record)
                    deleteTempIfCache(source)
                    _state.value = CreateState.Ready(r.record.id, difficulty)
                }
                ImportResult.TooSmall ->
                    _state.value = CreateState.Error("That photo is too small — try a larger one.")
                ImportResult.DecodeFailed ->
                    _state.value = CreateState.Error("Couldn't read that photo — try another.")
                ImportResult.IoFailed ->
                    _state.value = CreateState.Error("Something went wrong saving the puzzle.")
            }
        }
    }

    fun dismissError() { _state.value = CreateState.RequestingPermission }
    fun consumeReady() { _state.value = CreateState.Launching }

    fun cancel(source: Uri?) {
        source?.let { deleteTempIfCache(it) }
        _state.value = CreateState.Launching
    }

    private suspend fun customCountSnapshot(): Int {
        val list = puzzleRepository.observePuzzles().first()
        return list.count { it.source == PuzzleSource.CUSTOM }
    }

    private fun deleteTempIfCache(source: Uri) {
        val path = source.path ?: return
        val f = File(path)
        if (f.exists() && f.absolutePath.startsWith(context.cacheDir.absolutePath)) {
            runCatching { f.delete() }
        }
    }

    private companion object {
        const val MIN_GENERATING_MS = 500L
    }
}
