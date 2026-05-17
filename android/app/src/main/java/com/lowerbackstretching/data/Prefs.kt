package com.lowerbackstretching.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

object PrefKeys {
    val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
    val REMINDER_HOUR = intPreferencesKey("reminder_hour")
    val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
    val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val DURATION_UNIT = stringPreferencesKey("duration_unit")
    val HAPTICS_TRANSITIONS = booleanPreferencesKey("haptics_transitions")
    val HAPTICS_FINISH = booleanPreferencesKey("haptics_finish")
}

object ReminderDefaults {
    const val HOUR = 8
    const val MINUTE = 0
}

enum class ThemeMode(val storageValue: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromStorage(value: String?): ThemeMode =
            entries.firstOrNull { it.storageValue == value } ?: SYSTEM
    }
}

enum class DurationUnit(val storageValue: String) {
    SECONDS("seconds"),
    MINUTES_SHORT("minutes_short");

    companion object {
        fun fromStorage(value: String?): DurationUnit =
            entries.firstOrNull { it.storageValue == value } ?: SECONDS
    }
}

class Prefs(private val context: Context) {
    val reminderEnabled: Flow<Boolean> = context.dataStore.data.map { it[PrefKeys.REMINDER_ENABLED] ?: false }
    val reminderHour: Flow<Int> = context.dataStore.data.map { it[PrefKeys.REMINDER_HOUR] ?: ReminderDefaults.HOUR }
    val reminderMinute: Flow<Int> = context.dataStore.data.map { it[PrefKeys.REMINDER_MINUTE] ?: ReminderDefaults.MINUTE }
    val onboardingDone: Flow<Boolean> = context.dataStore.data.map { it[PrefKeys.ONBOARDING_DONE] ?: false }
    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { ThemeMode.fromStorage(it[PrefKeys.THEME_MODE]) }
    val durationUnit: Flow<DurationUnit> = context.dataStore.data.map { DurationUnit.fromStorage(it[PrefKeys.DURATION_UNIT]) }
    val hapticsTransitions: Flow<Boolean> = context.dataStore.data.map { it[PrefKeys.HAPTICS_TRANSITIONS] ?: true }
    val hapticsFinish: Flow<Boolean> = context.dataStore.data.map { it[PrefKeys.HAPTICS_FINISH] ?: true }

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

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[PrefKeys.THEME_MODE] = mode.storageValue }
    }

    suspend fun setDurationUnit(unit: DurationUnit) {
        context.dataStore.edit { it[PrefKeys.DURATION_UNIT] = unit.storageValue }
    }

    suspend fun setHapticsTransitions(enabled: Boolean) {
        context.dataStore.edit { it[PrefKeys.HAPTICS_TRANSITIONS] = enabled }
    }

    suspend fun setHapticsFinish(enabled: Boolean) {
        context.dataStore.edit { it[PrefKeys.HAPTICS_FINISH] = enabled }
    }

    /** Test helper — clears all keys so the next read returns defaults. */
    internal suspend fun resetForTests() {
        context.dataStore.edit { it.clear() }
    }
}
