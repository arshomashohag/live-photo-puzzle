package com.tessera.puzzle.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tessera.puzzle.domain.model.feedback.CompleteSound
import com.tessera.puzzle.domain.model.feedback.MoveSound
import com.tessera.puzzle.domain.model.persistence.Settings
import com.tessera.puzzle.domain.model.persistence.ThemeMode
import com.tessera.puzzle.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    override val settings: Flow<Settings> = dataStore.data.map { prefs ->
        Settings(
            soundEnabled = prefs[KEY_SOUND] ?: true,
            hapticsEnabled = prefs[KEY_HAPTICS] ?: true,
            theme = runCatching {
                ThemeMode.valueOf(prefs[KEY_THEME] ?: ThemeMode.SYSTEM.name)
            }.getOrDefault(ThemeMode.SYSTEM),
            guideShown = prefs[KEY_GUIDE_SHOWN] ?: false,
            moveSound = runCatching {
                MoveSound.valueOf(prefs[KEY_MOVE_SOUND] ?: MoveSound.SOFT_TICK.name)
            }.getOrDefault(MoveSound.SOFT_TICK),
            completeSound = runCatching {
                CompleteSound.valueOf(prefs[KEY_COMPLETE_SOUND] ?: CompleteSound.ARPEGGIO.name)
            }.getOrDefault(CompleteSound.ARPEGGIO),
        )
    }

    override suspend fun setSoundEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_SOUND] = enabled }
    }

    override suspend fun setHapticsEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_HAPTICS] = enabled }
    }

    override suspend fun setTheme(mode: ThemeMode) {
        dataStore.edit { it[KEY_THEME] = mode.name }
    }

    override suspend fun setGuideShown(shown: Boolean) {
        dataStore.edit { it[KEY_GUIDE_SHOWN] = shown }
    }

    override suspend fun setMoveSound(sound: MoveSound) {
        dataStore.edit { it[KEY_MOVE_SOUND] = sound.name }
    }

    override suspend fun setCompleteSound(sound: CompleteSound) {
        dataStore.edit { it[KEY_COMPLETE_SOUND] = sound.name }
    }

    private companion object {
        val KEY_SOUND = booleanPreferencesKey("sound_enabled")
        val KEY_HAPTICS = booleanPreferencesKey("haptics_enabled")
        val KEY_THEME = stringPreferencesKey("theme_mode")
        val KEY_GUIDE_SHOWN = booleanPreferencesKey("guide_shown")
        val KEY_MOVE_SOUND = stringPreferencesKey("move_sound")
        val KEY_COMPLETE_SOUND = stringPreferencesKey("complete_sound")
    }
}
