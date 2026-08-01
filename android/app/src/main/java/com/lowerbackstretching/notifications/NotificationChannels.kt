package com.lowerbackstretching.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.lowerbackstretching.R

object NotificationChannels {
    const val REMINDER = "stretch_reminders"

    // Notification channels are API 26+, which is our minSdk.
    fun registerAll(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                REMINDER,
                context.getString(R.string.notif_channel_reminders),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = context.getString(R.string.notif_channel_reminders_desc)
            },
        )
    }
}
