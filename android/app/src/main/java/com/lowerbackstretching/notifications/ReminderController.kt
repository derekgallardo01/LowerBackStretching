package com.lowerbackstretching.notifications

import android.content.Context
import com.lowerbackstretching.data.Prefs

/**
 * Single source of truth for "user changed the reminder". Persists the
 * preference and re-(scheduling|cancelling) the AlarmManager alarm so the
 * two never drift. Called by Settings and Onboarding.
 */
suspend fun Prefs.applyReminder(context: Context, enabled: Boolean, hour: Int, minute: Int) {
    setReminder(enabled, hour, minute)
    if (enabled) {
        ReminderScheduler.schedule(context, hour, minute)
    } else {
        ReminderScheduler.cancel(context)
    }
}

/**
 * Single source of truth for "user changed the streak-at-risk nudge."
 * Persists the preference and re-(scheduling|cancelling) the AlarmManager
 * alarm so the two never drift. The receiver gates posting per-day on
 * the current streak and whether the user already stretched today.
 */
suspend fun Prefs.applyStreakNudge(context: Context, enabled: Boolean) {
    setStreakNudgeEnabled(enabled)
    if (enabled) {
        ReminderScheduler.scheduleStreakNudge(context)
    } else {
        ReminderScheduler.cancelStreakNudge(context)
    }
}
