package com.lowerbackstretching.calendar

import android.content.Intent
import android.provider.CalendarContract
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CalendarIntentTest {

    @Test
    fun action_and_data_target_the_calendar_events_uri() {
        val intent = scheduleStretchBreakIntent()
        assertThat(intent.action).isEqualTo(Intent.ACTION_INSERT)
        assertThat(intent.data).isEqualTo(CalendarContract.Events.CONTENT_URI)
    }

    @Test
    fun default_title_and_duration_are_sensible() {
        val intent = scheduleStretchBreakIntent()
        assertThat(intent.getStringExtra(CalendarContract.Events.TITLE))
            .isEqualTo("Stretching break")
        val start = intent.getLongExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, 0)
        val end = intent.getLongExtra(CalendarContract.EXTRA_EVENT_END_TIME, 0)
        assertThat(end - start).isEqualTo(10 * 60 * 1000L)  // 10 min default
        assertThat(start).isGreaterThan(System.currentTimeMillis())
    }

    @Test
    fun custom_title_minutes_and_duration_propagate() {
        val intent = scheduleStretchBreakIntent(
            title = "Custom title",
            minutesFromNow = 30,
            durationMinutes = 5,
        )
        assertThat(intent.getStringExtra(CalendarContract.Events.TITLE))
            .isEqualTo("Custom title")
        val start = intent.getLongExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, 0)
        val end = intent.getLongExtra(CalendarContract.EXTRA_EVENT_END_TIME, 0)
        assertThat(end - start).isEqualTo(5 * 60 * 1000L)
        // 30-minute offset, allowing a few hundred ms for test setup.
        val expected = System.currentTimeMillis() + 30 * 60 * 1000L
        assertThat(start).isAtLeast(expected - 1000)
        assertThat(start).isAtMost(expected + 1000)
    }
}
