package com.tessera.puzzle.feedback

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.tessera.puzzle.domain.model.feedback.HapticKind
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Plays short haptics via [Vibrator]. App singleton. If the device has no
 * vibrator every call is a silent no-op (Resiliency). Uses predefined effects
 * where available and falls back to one-shots on older devices.
 */
@Singleton
class HapticPlayer @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val vibrator: Vibrator? = resolveVibrator(context)

    fun play(kind: HapticKind) {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        val effect = when (kind) {
            HapticKind.TICK -> tickEffect()
            HapticKind.SUCCESS -> VibrationEffect.createWaveform(
                SUCCESS_TIMINGS, SUCCESS_AMPLITUDES, -1,
            )
        }
        runCatching { v.vibrate(effect) }
    }

    private fun tickEffect(): VibrationEffect =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
        } else {
            VibrationEffect.createOneShot(TICK_MS, VibrationEffect.DEFAULT_AMPLITUDE)
        }

    private fun resolveVibrator(context: Context): Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(VibratorManager::class.java)
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

    private companion object {
        const val TICK_MS = 12L
        // Short two-pulse "success" pattern: pause, buzz, gap, stronger buzz.
        val SUCCESS_TIMINGS = longArrayOf(0, 30, 40, 60)
        val SUCCESS_AMPLITUDES = intArrayOf(0, 140, 0, 220)
    }
}
