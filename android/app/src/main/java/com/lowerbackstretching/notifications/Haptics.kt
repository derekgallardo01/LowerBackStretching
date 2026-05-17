package com.lowerbackstretching.notifications

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Thin wrapper around the platform vibrator API. Gated by the caller —
 * pass the user's preference for the relevant event before calling.
 */
object Haptics {

    fun short(context: Context) = vibrate(context, durationMillis = 30)

    fun finish(context: Context) = vibrate(context, durationMillis = 120)

    private fun vibrate(context: Context, durationMillis: Long) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        } ?: return
        if (!vibrator.hasVibrator()) return
        vibrator.vibrate(
            VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE)
        )
    }
}
