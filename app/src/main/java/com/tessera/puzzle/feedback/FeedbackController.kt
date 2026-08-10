package com.tessera.puzzle.feedback

import com.tessera.puzzle.di.ApplicationScope
import com.tessera.puzzle.domain.model.feedback.FeedbackDecider
import com.tessera.puzzle.domain.model.feedback.FeedbackEvent
import com.tessera.puzzle.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single entry point for game feedback. Holds the latest sound/haptics flags
 * (collected from [SettingsRepository]) and, for each event, asks the pure
 * [FeedbackDecider] what to play, then dispatches to the platform players.
 * Toggling a setting takes effect on the next event.
 */
@Singleton
class FeedbackController @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val soundPlayer: SoundPlayer,
    private val hapticPlayer: HapticPlayer,
    @ApplicationScope scope: CoroutineScope,
) {
    private data class Flags(val sound: Boolean, val haptics: Boolean)

    private val flags = settingsRepository.settings
        .map { Flags(it.soundEnabled, it.hapticsEnabled) }
        .stateIn(scope, SharingStarted.Eagerly, Flags(sound = true, haptics = true))

    fun onMove() = emit(FeedbackEvent.MOVE)

    fun onComplete() = emit(FeedbackEvent.COMPLETE)

    private fun emit(event: FeedbackEvent) {
        val f = flags.value
        val cue = FeedbackDecider.decide(event, f.sound, f.haptics)
        cue.sound?.let(soundPlayer::play)
        cue.haptic?.let(hapticPlayer::play)
    }
}
