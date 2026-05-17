package com.lowerbackstretching.calendar

import android.content.Intent
import android.provider.CalendarContract
import java.util.concurrent.TimeUnit

/**
 * Build an Intent that opens the system calendar app's "new event"
 * screen pre-filled with a stretching break. The user finishes the
 * create flow there; no calendar permissions are required because
 * we're just handing the data off via [Intent.ACTION_INSERT].
 *
 * Pure builder — no Context needed.
 */
fun scheduleStretchBreakIntent(
    title: String = "Stretching break",
    minutesFromNow: Int = 15,
    durationMinutes: Int = 10,
): Intent {
    val now = System.currentTimeMillis()
    val start = now + TimeUnit.MINUTES.toMillis(minutesFromNow.toLong())
    val end = start + TimeUnit.MINUTES.toMillis(durationMinutes.toLong())
    return Intent(Intent.ACTION_INSERT).apply {
        data = CalendarContract.Events.CONTENT_URI
        putExtra(CalendarContract.Events.TITLE, title)
        putExtra(CalendarContract.Events.DESCRIPTION, "Take a few minutes to stretch.")
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start)
        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end)
    }
}
