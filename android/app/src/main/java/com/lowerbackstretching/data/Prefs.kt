package com.lowerbackstretching.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

object PrefKeys {
    val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
    val REMINDER_HOUR = intPreferencesKey("reminder_hour")
    val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
    val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
}

object ReminderDefaults {
    const val HOUR = 8
    const val MINUTE = 0
}

class Prefs(private val context: Context) {
    val reminderEnabled: Flow<Boolean> = context.dataStore.data.map { it[PrefKeys.REMINDER_ENABLED] ?: false }
    val reminderHour: Flow<Int> = context.dataStore.data.map { it[PrefKeys.REMINDER_HOUR] ?: ReminderDefaults.HOUR }
    val reminderMinute: Flow<Int> = context.dataStore.data.map { it[PrefKeys.REMINDER_MINUTE] ?: ReminderDefaults.MINUTE }
    val onboardingDone: Flow<Boolean> = context.dataStore.data.map { it[PrefKeys.ONBOARDING_DONE] ?: false }

    internal suspend fun setReminder(enabled: Boolean, hour: Int, minute: Int) {
        context.dataStore.edit {
            it[PrefKeys.REMINDER_ENABLED] = enabled
            it[PrefKeys.REMINDER_HOUR] = hour
            it[PrefKeys.REMINDER_MINUTE] = minute
        }
    }

    suspend fun markOnboardingDone() {
        context.dataStore.edit { it[PrefKeys.ONBOARDING_DONE] = true }
    }

    /** Test helper — clears all keys so the next read returns defaults. */
    internal suspend fun resetForTests() {
        context.dataStore.edit { it.clear() }
    }
}
