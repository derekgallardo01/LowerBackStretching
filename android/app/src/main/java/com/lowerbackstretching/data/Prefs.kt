package com.lowerbackstretching.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lowerbackstretching.audio.AmbientTrack
import com.lowerbackstretching.audio.AudioDefaults
import com.lowerbackstretching.audio.ChimeTrack
import com.lowerbackstretching.audio.MusicTrack
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
    val IN_PROGRESS_PROGRAM_ID = stringPreferencesKey("in_progress_program_id")
    val IN_PROGRESS_DAY = intPreferencesKey("in_progress_day")
    val IN_PROGRESS_INDEX = intPreferencesKey("in_progress_index")
    val MUSIC_TRACK = stringPreferencesKey("music_track")
    val MUSIC_VOLUME = floatPreferencesKey("music_volume")
    val AMBIENT_TRACK = stringPreferencesKey("ambient_track")
    val AMBIENT_VOLUME = floatPreferencesKey("ambient_volume")
    val CHIME_TRACK = stringPreferencesKey("chime_track")
    val LAST_SESSION_EPOCH_DAY = longPreferencesKey("last_session_epoch_day")
    val WEEKLY_GOAL = intPreferencesKey("weekly_goal")
    val MONTHLY_GOAL = intPreferencesKey("monthly_goal")
    val HEALTH_WRITE_ENABLED = booleanPreferencesKey("health_write_enabled")
    val HEALTH_READ_ENABLED = booleanPreferencesKey("health_read_enabled")
}

object GoalDefaults {
    const val WEEKLY = 3
    const val MONTHLY = 12
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
    val inProgressSession: Flow<InProgressSession?> = context.dataStore.data.map { prefs ->
        val pid = prefs[PrefKeys.IN_PROGRESS_PROGRAM_ID] ?: return@map null
        InProgressSession(
            programId = pid,
            dayNumber = prefs[PrefKeys.IN_PROGRESS_DAY] ?: 0,
            index = prefs[PrefKeys.IN_PROGRESS_INDEX] ?: 0,
        )
    }
    val musicTrack: Flow<MusicTrack> = context.dataStore.data.map { MusicTrack.fromStorage(it[PrefKeys.MUSIC_TRACK]) }
    val musicVolume: Flow<Float> = context.dataStore.data.map { it[PrefKeys.MUSIC_VOLUME] ?: AudioDefaults.MUSIC_VOLUME }
    val ambientTrack: Flow<AmbientTrack> = context.dataStore.data.map { AmbientTrack.fromStorage(it[PrefKeys.AMBIENT_TRACK]) }
    val ambientVolume: Flow<Float> = context.dataStore.data.map { it[PrefKeys.AMBIENT_VOLUME] ?: AudioDefaults.AMBIENT_VOLUME }
    val chimeTrack: Flow<ChimeTrack> = context.dataStore.data.map { ChimeTrack.fromStorage(it[PrefKeys.CHIME_TRACK]) }
    val lastSessionEpochDay: Flow<Long> = context.dataStore.data.map { it[PrefKeys.LAST_SESSION_EPOCH_DAY] ?: 0L }
    val weeklyGoal: Flow<Int> = context.dataStore.data.map { it[PrefKeys.WEEKLY_GOAL] ?: GoalDefaults.WEEKLY }
    val monthlyGoal: Flow<Int> = context.dataStore.data.map { it[PrefKeys.MONTHLY_GOAL] ?: GoalDefaults.MONTHLY }
    val healthWriteEnabled: Flow<Boolean> = context.dataStore.data.map { it[PrefKeys.HEALTH_WRITE_ENABLED] ?: false }
    val healthReadEnabled: Flow<Boolean> = context.dataStore.data.map { it[PrefKeys.HEALTH_READ_ENABLED] ?: false }

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

    suspend fun saveInProgress(session: InProgressSession) {
        context.dataStore.edit {
            it[PrefKeys.IN_PROGRESS_PROGRAM_ID] = session.programId
            it[PrefKeys.IN_PROGRESS_DAY] = session.dayNumber
            it[PrefKeys.IN_PROGRESS_INDEX] = session.index
        }
    }

    suspend fun clearInProgress() {
        context.dataStore.edit {
            it.remove(PrefKeys.IN_PROGRESS_PROGRAM_ID)
            it.remove(PrefKeys.IN_PROGRESS_DAY)
            it.remove(PrefKeys.IN_PROGRESS_INDEX)
        }
    }

    suspend fun setMusicTrack(track: MusicTrack) {
        context.dataStore.edit { it[PrefKeys.MUSIC_TRACK] = track.storageValue }
    }

    suspend fun setMusicVolume(volume: Float) {
        context.dataStore.edit { it[PrefKeys.MUSIC_VOLUME] = volume.coerceIn(0f, 1f) }
    }

    suspend fun setAmbientTrack(track: AmbientTrack) {
        context.dataStore.edit { it[PrefKeys.AMBIENT_TRACK] = track.storageValue }
    }

    suspend fun setAmbientVolume(volume: Float) {
        context.dataStore.edit { it[PrefKeys.AMBIENT_VOLUME] = volume.coerceIn(0f, 1f) }
    }

    suspend fun setChimeTrack(track: ChimeTrack) {
        context.dataStore.edit { it[PrefKeys.CHIME_TRACK] = track.storageValue }
    }

    suspend fun setLastSessionEpochDay(epochDay: Long) {
        context.dataStore.edit { it[PrefKeys.LAST_SESSION_EPOCH_DAY] = epochDay }
    }

    suspend fun setWeeklyGoal(target: Int) {
        context.dataStore.edit { it[PrefKeys.WEEKLY_GOAL] = target.coerceIn(1, 21) }
    }

    suspend fun setMonthlyGoal(target: Int) {
        context.dataStore.edit { it[PrefKeys.MONTHLY_GOAL] = target.coerceIn(1, 90) }
    }

    suspend fun setHealthWriteEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PrefKeys.HEALTH_WRITE_ENABLED] = enabled }
    }

    suspend fun setHealthReadEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PrefKeys.HEALTH_READ_ENABLED] = enabled }
    }

    /** Test helper — clears all keys so the next read returns defaults. */
    internal suspend fun resetForTests() {
        context.dataStore.edit { it.clear() }
    }
}
