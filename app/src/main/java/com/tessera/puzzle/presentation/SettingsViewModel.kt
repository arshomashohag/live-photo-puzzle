package com.tessera.puzzle.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tessera.puzzle.domain.model.feedback.CompleteSound
import com.tessera.puzzle.domain.model.feedback.MoveSound
import com.tessera.puzzle.domain.model.persistence.Settings
import com.tessera.puzzle.domain.model.persistence.ThemeMode
import com.tessera.puzzle.domain.repository.SettingsRepository
import com.tessera.puzzle.domain.repository.StatsRepository
import com.tessera.puzzle.feedback.SoundPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val statsRepository: StatsRepository,
    private val soundPlayer: SoundPlayer,
) : ViewModel() {

    val settings: StateFlow<Settings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Settings())

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setTheme(mode) }
    }

    fun setSound(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setSoundEnabled(enabled) }
    }

    fun setHaptics(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setHapticsEnabled(enabled) }
    }

    /** Select a move sound and play it once so the choice is audible. */
    fun setMoveSound(sound: MoveSound) {
        soundPlayer.previewMove(sound)
        viewModelScope.launch { settingsRepository.setMoveSound(sound) }
    }

    /** Select a completion sound and preview it. */
    fun setCompleteSound(sound: CompleteSound) {
        soundPlayer.previewComplete(sound)
        viewModelScope.launch { settingsRepository.setCompleteSound(sound) }
    }

    fun resetStats() {
        viewModelScope.launch { statsRepository.resetAll() }
    }
}
