package com.lowerbackstretching.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.lowerbackstretching.MainActivity
import com.lowerbackstretching.R
import com.lowerbackstretching.data.Prefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent?,
    ) {
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!shouldSuppressToday(appContext)) {
                    postReminder(appContext)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun postReminder(context: Context) {
        val openApp = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val content = PendingIntent.getActivity(
            context,
            0,
            openApp,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = NotificationCompat
            .Builder(context, NotificationChannels.REMINDER)
            .setSmallIcon(R.drawable.ic_stat_stretch)
            .setContentTitle(context.getString(R.string.notif_reminder_title))
            .setContentText(context.getString(R.string.notif_reminder_body))
            .setAutoCancel(true)
            .setContentIntent(content)
            .build()

        context.notifyIfPermitted(1, notification)
    }

    /**
     * Smart-reminder gate: skip if the user already stretched today.
     *
     * Reads [Prefs.lastSessionEpochDay] off the main thread. A cold DataStore
     * read touches disk, and `onReceive` runs on the main thread, so this used
     * to be an ANR surface on a slow device — [goAsync] plus an IO dispatch
     * removes it without changing behaviour.
     */
    private suspend fun shouldSuppressToday(context: Context): Boolean {
        val last = Prefs(context).lastSessionEpochDay.first()
        return last == LocalDate.now().toEpochDay()
    }
}
