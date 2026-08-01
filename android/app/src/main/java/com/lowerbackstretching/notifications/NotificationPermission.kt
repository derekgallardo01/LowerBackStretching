package com.lowerbackstretching.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/**
 * Post a notification only when we're actually allowed to.
 *
 * `areNotificationsEnabled()` covers the user muting the channel, but on API 33+
 * POST_NOTIFICATIONS is a runtime permission that can be denied independently —
 * and `notify` throws if it is. Checking both is what makes this safe (and is
 * what Lint's MissingPermission check is asking for).
 */
internal fun Context.notifyIfPermitted(
    id: Int,
    notification: android.app.Notification,
) {
    val granted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED
    if (!granted) return
    val manager = NotificationManagerCompat.from(this)
    if (!manager.areNotificationsEnabled()) return
    manager.notify(id, notification)
}
