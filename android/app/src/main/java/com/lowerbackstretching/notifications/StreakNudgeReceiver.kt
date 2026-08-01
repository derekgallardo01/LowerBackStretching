package com.lowerbackstretching.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.lowerbackstretching.App
import com.lowerbackstretching.MainActivity
import com.lowerbackstretching.R
import com.lowerbackstretching.core.computeStreak
import com.lowerbackstretching.data.Prefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
 * Reading the streak needs DataStore + a DAO hit. `onReceive` runs on the main
 * thread, so the work is dispatched to IO and the broadcast is kept alive with
 * [goAsync] until it completes — `goAsync()` extends the receiver's lifetime but
 * does not move work off the main thread, so the dispatch is the part that
 * matters.
 */
class StreakNudgeReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent?,
    ) {
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Resolve the streak once and reuse it: the gate needs it and so
                // does the notification copy.
                val streak = currentStreak(appContext)
                if (shouldNotify(appContext, streak)) {
                    postNotification(appContext, streak)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun shouldNotify(
        context: Context,
        streak: Int,
    ): Boolean {
        val prefs = Prefs(context)
        return shouldNudgeStreak(
            enabled = prefs.streakNudgeEnabled.first(),
            lastSessionEpochDay = prefs.lastSessionEpochDay.first(),
            today = LocalDate.now(),
            streakProvider = { streak },
        )
    }

    private suspend fun currentStreak(context: Context): Int {
        val app = context.applicationContext as App
        val days = app.sessionRepository.completedDays().first()
        return computeStreak(days, LocalDate.now())
    }

    private fun postNotification(
        context: Context,
        streak: Int,
    ) {
        // (Body unchanged; pulled out so the gating logic above is unit-testable.)
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
            .setContentTitle(context.getString(R.string.notif_streak_title, streak))
            .setContentText(context.getString(R.string.notif_streak_body))
            .setAutoCancel(true)
            .setContentIntent(content)
            .build()

        context.notifyIfPermitted(2, notification)
    }
}
