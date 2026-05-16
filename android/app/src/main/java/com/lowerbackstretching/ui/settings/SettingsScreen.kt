package com.lowerbackstretching.ui.settings

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.notifications.applyReminder
import com.lowerbackstretching.ui.AppViewModel
import com.lowerbackstretching.ui.util.formatTime
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(vm: AppViewModel = viewModel()) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val enabled by vm.prefs.reminderEnabled.collectAsState(initial = false)
    val hour by vm.prefs.reminderHour.collectAsState(initial = 8)
    val minute by vm.prefs.reminderMinute.collectAsState(initial = 0)

    fun applyReminder(enabled: Boolean, hour: Int, minute: Int) {
        scope.launch { vm.prefs.applyReminder(ctx, enabled, hour, minute) }
    }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)

        Card(shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(16.dp)) {
                ReminderToggleRow(
                    enabled = enabled,
                    onToggle = { on -> applyReminder(on, hour, minute) },
                )
                ReminderTimeRow(
                    formatted = formatTime(hour, minute),
                    onClick = {
                        TimePickerDialog(ctx, { _, h, m -> applyReminder(enabled, h, m) },
                            hour, minute, true).show()
                    },
                )
            }
        }

        AboutCard()
    }
}

@Composable
private fun ReminderToggleRow(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.padding(end = 12.dp)) {
            Text("Daily reminder", style = MaterialTheme.typography.titleMedium)
            Text(
                "A nudge to do your routine.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
        }
        Switch(checked = enabled, onCheckedChange = onToggle)
    }
}

@Composable
private fun ReminderTimeRow(formatted: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp).clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text("Reminder time", style = MaterialTheme.typography.titleMedium)
        Text(formatted, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun AboutCard() {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("About", style = MaterialTheme.typography.titleMedium)
            Text(
                "Lower Back Stretching · v0.1",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
        }
    }
}
