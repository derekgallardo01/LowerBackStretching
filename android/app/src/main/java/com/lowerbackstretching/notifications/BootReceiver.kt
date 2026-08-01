package com.lowerbackstretching.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lowerbackstretching.data.Prefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent?,
    ) {
        // BOOT_COMPLETED and MY_PACKAGE_REPLACED are protected broadcasts, but an
        // explicit intent with no action (or a different one) can still reach an
        // exported receiver. Verify the action rather than rescheduling on
        // anything that arrives.
        if (intent?.action !in HANDLED_ACTIONS) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val prefs = Prefs(context)
                if (prefs.reminderEnabled.first()) {
                    val hour = prefs.reminderHour.first()
                    val minute = prefs.reminderMinute.first()
                    ReminderScheduler.schedule(context, hour, minute)
                }
                if (prefs.streakNudgeEnabled.first()) {
                    ReminderScheduler.scheduleStreakNudge(context)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object {
        val HANDLED_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
        )
    }
}
