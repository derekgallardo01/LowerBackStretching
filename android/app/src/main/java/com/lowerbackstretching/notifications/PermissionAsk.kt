package com.lowerbackstretching.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat

/**
 * Returns a launcher that asks for POST_NOTIFICATIONS only when called,
 * and only on Android 13+. Used by Settings and Onboarding when the user
 * enables a reminder. Replaces the old on-create permission ask in
 * MainActivity that blocked Compose hierarchy attachment in tests.
 */
@Composable
fun rememberNotificationPermissionAsk(): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* no-op: scheduling proceeds regardless; OS will silently drop if denied */ }

    return remember(launcher) {
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

/** Headless check — useful in non-Compose contexts (e.g. tests). */
fun isNotificationPermissionGranted(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
    return ContextCompat.checkSelfPermission(
        context, Manifest.permission.POST_NOTIFICATIONS,
    ) == PackageManager.PERMISSION_GRANTED
}
