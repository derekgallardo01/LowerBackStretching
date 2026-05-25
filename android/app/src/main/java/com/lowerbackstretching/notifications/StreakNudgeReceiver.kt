package com.lowerbackstretching.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.lowerbackstretching.App
import com.lowerbackstretching.MainActivity
import com.lowerbackstretching.R
import com.lowerbackstretching.core.computeStreak
import com.lowerbackstretching.data.Prefs
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

/**
 * Pure gating logic for the streak-at-risk notification: returns true
 * when the user has opted in, hasn't already stretched today, AND has
 * an active streak of at least 3 days. The streak lookup is suspended
 * via [streakProvider] so callers can plumb in DB I/O without dragging
 * the dependency into the test boundary.
 */
internal suspend fun shouldNudgeStreak(
    enabled: Boolean,
    lastSessionEpochDay: Long,
    today: LocalDate,
    streakProvider: suspend () -> Int,
): Boolean {
    if (!enabled) return false
    if (lastSessionEpochDay == today.toEpochDay()) return false
    return streakProvider() >= 3
}

/**
 * Fires daily at 20:00 (scheduled by [ReminderScheduler.scheduleStreakNudge]).
 * Only posts a notification when:
 *  - The user has opted in (`streakNudgeEnabled` is true), AND
 *  - No session has been completed today, AND
 *  - The current streak is at least 3 days (so we don't spam users who
 *    barely started).
 *
 * Reading the streak requires a quick DAO hit; we runBlocking on the
 * receiver's main-thread budget. DataStore + Room return fast enough
 * that this is safe.
 */
class StreakNudgeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        try {
            runBlocking {
                if (!shouldNotify(context)) return@runBlocking
                postNotification(context, currentStreak(context))
            }
        } finally {
            pendingResult.finish()
        }
    }

    private suspend fun shouldNotify(context: Context): Boolean {
        val prefs = Prefs(context)
        return shouldNudgeStreak(
            enabled = prefs.streakNudgeEnabled.first(),
            lastSessionEpochDay = prefs.lastSessionEpochDay.first(),
            today = LocalDate.now(),
            streakProvider = { currentStreak(context) },
        )
    }

    private suspend fun currentStreak(context: Context): Int {
        val app = context.applicationContext as App
        val days = app.sessionRepository.completedDays().first()
        return computeStreak(days, LocalDate.now())
    }

    private fun postNotification(context: Context, streak: Int) {
        // (Body unchanged; pulled out so the gating logic above is unit-testable.)
        val openApp = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val content = PendingIntent.getActivity(
            context, 0, openApp,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, NotificationChannels.REMINDER)
            .setSmallIcon(R.drawable.ic_stat_stretch)
            .setContentTitle("Your $streak-day streak is hanging on")
            .setContentText("Three minutes is enough to keep it alive.")
            .setAutoCancel(true)
            .setContentIntent(content)
            .build()

        NotificationManagerCompat.from(context)
            .takeIf { it.areNotificationsEnabled() }
            ?.notify(2, notification)
    }
}
