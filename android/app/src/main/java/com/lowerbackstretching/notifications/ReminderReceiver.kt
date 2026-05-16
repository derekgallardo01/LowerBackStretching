package com.lowerbackstretching.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.lowerbackstretching.MainActivity
import com.lowerbackstretching.R

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val openApp = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val content = PendingIntent.getActivity(
            context, 0, openApp,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = NotificationCompat.Builder(context, NotificationChannels.REMINDER)
            .setSmallIcon(R.drawable.ic_stat_stretch)
            .setContentTitle("Time to stretch")
            .setContentText("A few minutes today keeps your back happy.")
            .setAutoCancel(true)
            .setContentIntent(content)
            .build()

        NotificationManagerCompat.from(context)
            .takeIf { it.areNotificationsEnabled() }
            ?.notify(1, notification)
    }
}
