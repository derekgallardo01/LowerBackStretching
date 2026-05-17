package com.lowerbackstretching.wear

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Tiny on-wrist nudges. The watch uses these instead of audio (the
 * speaker isn't reliable on every device) so the wearer feels the
 * transition between stretches even when their wrist is down.
 */
object WearHaptics {

    fun short(context: Context) = vibrate(context, durationMs = 60)
    fun finish(context: Context) = vibrate(context, durationMs = 220)

    private fun vibrate(context: Context, durationMs: Long) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)
                ?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        } ?: return
        vibrator.vibrate(
            VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
        )
    }
}
