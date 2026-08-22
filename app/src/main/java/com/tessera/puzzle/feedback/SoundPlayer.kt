package com.tessera.puzzle.feedback

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import com.tessera.puzzle.R
import com.tessera.puzzle.domain.model.feedback.CompleteSound
import com.tessera.puzzle.domain.model.feedback.MoveSound
import com.tessera.puzzle.domain.model.feedback.SoundClip
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Low-latency SFX via [SoundPool] (pre-loaded, respects the media volume). App
 * singleton; [release] frees the pool. Every selectable move/completion variant
 * is loaded up front and played by its enum. Any load/play failure is a silent
 * no-op so gameplay never breaks (Resiliency / SECURITY-15 cleanup).
 */
@Singleton
class SoundPlayer @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(MAX_STREAMS)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private val moveIds = mutableMapOf<MoveSound, Int>()
    private val completeIds = mutableMapOf<CompleteSound, Int>()

    init {
        runCatching {
            MoveSound.entries.forEach { moveIds[it] = pool.load(context, rawFor(it), 1) }
            CompleteSound.entries.forEach { completeIds[it] = pool.load(context, rawFor(it), 1) }
        }.onFailure { Log.w(TAG, "SFX load failed; sound disabled") }
    }

    /** Play the resolved [clip]; no-op if it failed to load. */
    fun play(clip: SoundClip) {
        val id = when (clip) {
            is SoundClip.Move -> moveIds[clip.variant]
            is SoundClip.Complete -> completeIds[clip.variant]
        } ?: return
        runCatching { pool.play(id, 1f, 1f, 1, 0, 1f) }
    }

    /** Preview a move variant (drawer selection), regardless of the play flags. */
    fun previewMove(variant: MoveSound) = play(SoundClip.Move(variant))

    /** Preview a completion variant (drawer selection). */
    fun previewComplete(variant: CompleteSound) = play(SoundClip.Complete(variant))

    fun release() {
        runCatching { pool.release() }
    }

    private fun rawFor(variant: MoveSound): Int = when (variant) {
        MoveSound.SOFT_TICK -> R.raw.sfx_move_soft_tick
        MoveSound.POP -> R.raw.sfx_move_pop
        MoveSound.CLICK -> R.raw.sfx_move_click
        MoveSound.MARIMBA -> R.raw.sfx_move_marimba
        MoveSound.GLASS -> R.raw.sfx_move_glass
    }

    private fun rawFor(variant: CompleteSound): Int = when (variant) {
        CompleteSound.ARPEGGIO -> R.raw.sfx_complete_arpeggio
        CompleteSound.SPARKLE -> R.raw.sfx_complete_sparkle
        CompleteSound.CHIME -> R.raw.sfx_complete_chime
        CompleteSound.FANFARE -> R.raw.sfx_complete_fanfare
    }

    private companion object {
        const val TAG = "SoundPlayer"
        const val MAX_STREAMS = 4
    }
}
