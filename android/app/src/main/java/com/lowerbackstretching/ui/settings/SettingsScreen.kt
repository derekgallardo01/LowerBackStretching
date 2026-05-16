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
import com.lowerbackstretching.notifications.ReminderScheduler
import com.lowerbackstretching.ui.AppViewModel
import kotlinx.coroutines.launch


@Composable
fun SettingsScreen(vm: AppViewModel = viewModel()) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val enabled by vm.prefs.reminderEnabled.collectAsState(initial = false)
    val hour by vm.prefs.reminderHour.collectAsState(initial = 8)
    val minute by vm.prefs.reminderMinute.collectAsState(initial = 0)

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)

        Card(shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.padding(end = 12.dp)) {
                        Text("Daily reminder", style = MaterialTheme.typography.titleMedium)
                        Text("A nudge to do your routine.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                    Switch(
                        checked = enabled,
                        onCheckedChange = { on ->
                            scope.launch {
                                vm.prefs.setReminder(on, hour, minute)
                                if (on) ReminderScheduler.schedule(ctx, hour, minute)
                                else ReminderScheduler.cancel(ctx)
                            }
                        }
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clickable {
                            TimePickerDialog(ctx, { _, h, m ->
                                scope.launch {
                                    vm.prefs.setReminder(enabled, h, m)
                                    if (enabled) ReminderScheduler.schedule(ctx, h, m)
                                }
                            }, hour, minute, true).show()
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Reminder time", style = MaterialTheme.typography.titleMedium)
                    Text(String.format("%02d:%02d", hour, minute), style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        Card(shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("About", style = MaterialTheme.typography.titleMedium)
                Text("Lower Back Stretching · v0.1",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
        }
    }
}
