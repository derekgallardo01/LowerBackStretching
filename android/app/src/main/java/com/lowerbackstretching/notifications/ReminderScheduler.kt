package com.lowerbackstretching.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object ReminderScheduler {

    private const val REQUEST_CODE = 1001

    fun schedule(context: Context, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pending = pendingIntent(context, create = true)
        val triggerAt = nextOccurrence(hour, minute, Calendar.getInstance())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP, triggerAt, AlarmManager.INTERVAL_DAY, pending
            )
        } else {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP, triggerAt, AlarmManager.INTERVAL_DAY, pending
            )
        }
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        pendingIntent(context, create = false)?.let { alarmManager.cancel(it) }
    }

    private fun pendingIntent(context: Context, create: Boolean): PendingIntent? {
        val intent = Intent(context, ReminderReceiver::class.java)
        val flags = PendingIntent.FLAG_IMMUTABLE or
            (if (create) PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_NO_CREATE)
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intent, flags)
    }
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
