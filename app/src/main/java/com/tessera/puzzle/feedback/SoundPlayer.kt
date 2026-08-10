package com.tessera.puzzle.feedback

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import com.tessera.puzzle.R
import com.tessera.puzzle.domain.model.feedback.SoundKind
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Low-latency SFX via [SoundPool] (pre-loaded, respects the media volume). App
 * singleton; [release] frees the pool. Any load/play failure is a silent no-op
 * so gameplay never breaks (Resiliency / SECURITY-15 cleanup).
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

    private val soundIds = mutableMapOf<SoundKind, Int>()

    init {
        runCatching {
            soundIds[SoundKind.MOVE] = pool.load(context, R.raw.sfx_move, 1)
            soundIds[SoundKind.COMPLETE] = pool.load(context, R.raw.sfx_complete, 1)
        }.onFailure { Log.w(TAG, "SFX load failed; sound disabled") }
    }

    /** Play [kind]; no-op if it failed to load. */
    fun play(kind: SoundKind) {
        val id = soundIds[kind] ?: return
        runCatching { pool.play(id, 1f, 1f, 1, 0, 1f) }
    }

    fun release() {
        runCatching { pool.release() }
    }

    private companion object {
        const val TAG = "SoundPlayer"
        const val MAX_STREAMS = 4
    }
}
