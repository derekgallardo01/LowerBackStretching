package com.lowerbackstretching.notifications

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.lowerbackstretching.data.Prefs
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * `Prefs.applyReminder` should always persist the preference, regardless of
 * the scheduler's success. Scheduling itself talks to AlarmManager which we
 * don't assert on here — the unit test for `nextOccurrence` covers the math
 * and the runtime side is hard to assert without a flaky timing test.
 */
@RunWith(AndroidJUnit4::class)
class ReminderControllerTest {

    private val ctx = InstrumentationRegistry.getInstrumentation().targetContext
    private val prefs = Prefs(ctx)

    @Before
    fun reset() {
        File(ctx.filesDir, "datastore/settings.preferences_pb").delete()
    }

    @Test
    fun applyReminder_enabled_persists_all_fields() = runBlocking {
        prefs.applyReminder(ctx, enabled = true, hour = 9, minute = 15)
        assertThat(prefs.reminderEnabled.first()).isTrue()
        assertThat(prefs.reminderHour.first()).isEqualTo(9)
        assertThat(prefs.reminderMinute.first()).isEqualTo(15)
    }

    @Test
    fun applyReminder_disabled_persists_time_but_disables() = runBlocking {
        prefs.applyReminder(ctx, enabled = true, hour = 9, minute = 15)
        prefs.applyReminder(ctx, enabled = false, hour = 9, minute = 15)
        assertThat(prefs.reminderEnabled.first()).isFalse()
        assertThat(prefs.reminderHour.first()).isEqualTo(9)
    }
}
