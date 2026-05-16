package com.lowerbackstretching.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PrefsTest {

    private val ctx = InstrumentationRegistry.getInstrumentation().targetContext
    private val prefs = Prefs(ctx)

    @Before
    fun reset() = runBlocking {
        prefs.resetForTests()
    }

    @Test
    fun defaults_when_nothing_has_been_written() = runBlocking {
        assertThat(prefs.reminderEnabled.first()).isFalse()
        assertThat(prefs.reminderHour.first()).isEqualTo(ReminderDefaults.HOUR)
        assertThat(prefs.reminderMinute.first()).isEqualTo(ReminderDefaults.MINUTE)
        assertThat(prefs.onboardingDone.first()).isFalse()
    }

    @Test
    fun setReminder_persists_all_three_values() = runBlocking {
        prefs.setReminder(enabled = true, hour = 7, minute = 30)
        assertThat(prefs.reminderEnabled.first()).isTrue()
        assertThat(prefs.reminderHour.first()).isEqualTo(7)
        assertThat(prefs.reminderMinute.first()).isEqualTo(30)
    }

    @Test
    fun setReminder_overwrites_previous_values() = runBlocking {
        prefs.setReminder(enabled = true, hour = 7, minute = 30)
        prefs.setReminder(enabled = false, hour = 21, minute = 0)
        assertThat(prefs.reminderEnabled.first()).isFalse()
        assertThat(prefs.reminderHour.first()).isEqualTo(21)
        assertThat(prefs.reminderMinute.first()).isEqualTo(0)
    }

    @Test
    fun markOnboardingDone_flips_the_flag() = runBlocking {
        assertThat(prefs.onboardingDone.first()).isFalse()
        prefs.markOnboardingDone()
        assertThat(prefs.onboardingDone.first()).isTrue()
    }
}
