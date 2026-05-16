package com.lowerbackstretching.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object ReminderScheduler {

    private const val REQUEST_CODE = 1001

    fun schedule(context: Context, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            nextOccurrence(hour, minute, Calendar.getInstance()),
            AlarmManager.INTERVAL_DAY,
            createPendingIntent(context),
        )
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        existingPendingIntent(context)?.let { alarmManager.cancel(it) }
    }

    /** Creates or updates the broadcast PendingIntent. Never null. */
    private fun createPendingIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            Intent(context, ReminderReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

    /** Returns the existing PendingIntent (for cancellation) or null if none. */
    private fun existingPendingIntent(context: Context): PendingIntent? =
        PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            Intent(context, ReminderReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE,
        )
}

/**
 * Compute the next epoch-millis at which [hour]:[minute] occurs given a
 * `now` calendar. If `hour:minute` has already passed today, returns the
 * occurrence one day later. Pure — extracted from [ReminderScheduler] for
 * unit testing.
 */
internal fun nextOccurrence(hour: Int, minute: Int, now: Calendar): Long {
    val target = (now.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    if (target.timeInMillis <= now.timeInMillis) {
        target.add(Calendar.DAY_OF_YEAR, 1)
    }
    return target.timeInMillis
}
