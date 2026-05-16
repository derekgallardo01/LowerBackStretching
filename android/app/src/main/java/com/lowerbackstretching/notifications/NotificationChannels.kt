package com.lowerbackstretching.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val REMINDER = "stretch_reminders"

    fun registerAll(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(
                    REMINDER,
                    "Stretching reminders",
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply { description = "Daily nudges to do your stretching routine" }
            )
        }
    }
}
