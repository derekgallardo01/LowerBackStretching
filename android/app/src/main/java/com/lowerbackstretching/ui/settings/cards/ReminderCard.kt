package com.lowerbackstretching.ui.settings.cards

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.notifications.applyReminder
import com.lowerbackstretching.notifications.rememberNotificationPermissionAsk
import com.lowerbackstretching.ui.AppViewModel
import com.lowerbackstretching.ui.util.formatTime
import kotlinx.coroutines.launch

@Composable
fun ReminderCard(vm: AppViewModel = viewModel()) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val askNotificationPermission = rememberNotificationPermissionAsk()
    val enabled by vm.prefs.reminderEnabled.collectAsState(initial = false)
    val hour by vm.prefs.reminderHour.collectAsState(initial = 8)
    val minute by vm.prefs.reminderMinute.collectAsState(initial = 0)

    fun apply(enabled: Boolean, hour: Int, minute: Int) {
        if (enabled) askNotificationPermission()
        scope.launch { vm.prefs.applyReminder(ctx, enabled, hour, minute) }
    }

    SettingsCard(verticalSpacing = 0.dp) {
        SettingsToggleRow(
            checked = enabled,
            onChange = { on -> apply(on, hour, minute) },
        ) {
            Text("Daily reminder", style = MaterialTheme.typography.titleMedium)
            Text(
                "A nudge to do your routine.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
        }
        TimeRow(
            formatted = formatTime(hour, minute),
            onClick = {
                TimePickerDialog(ctx, { _, h, m -> apply(enabled, h, m) },
                    hour, minute, true).show()
            },
        )
    }
}

@Composable
private fun TimeRow(formatted: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp).clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text("Reminder time", style = MaterialTheme.typography.titleMedium)
        Text(formatted, style = MaterialTheme.typography.titleMedium)
    }
}
