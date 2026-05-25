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
    override fun onReceive(context: Context, intent: Intent?) {
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
}
