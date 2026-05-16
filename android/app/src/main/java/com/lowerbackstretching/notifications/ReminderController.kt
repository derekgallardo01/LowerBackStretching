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
