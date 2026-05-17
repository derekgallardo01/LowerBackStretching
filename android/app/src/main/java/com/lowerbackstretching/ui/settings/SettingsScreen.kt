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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import com.lowerbackstretching.data.DurationUnit
import com.lowerbackstretching.data.ThemeMode
import com.lowerbackstretching.notifications.applyReminder
import com.lowerbackstretching.notifications.rememberNotificationPermissionAsk
import com.lowerbackstretching.ui.AppViewModel
import com.lowerbackstretching.ui.components.ScreenHeader
import com.lowerbackstretching.ui.components.SectionHeader
import com.lowerbackstretching.ui.util.formatTime
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(vm: AppViewModel = viewModel()) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val askNotificationPermission = rememberNotificationPermissionAsk()
    val enabled by vm.prefs.reminderEnabled.collectAsState(initial = false)
    val hour by vm.prefs.reminderHour.collectAsState(initial = 8)
    val minute by vm.prefs.reminderMinute.collectAsState(initial = 0)
    val themeMode by vm.prefs.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
    val durationUnit by vm.prefs.durationUnit.collectAsState(initial = DurationUnit.SECONDS)
    val hapticsTransitions by vm.prefs.hapticsTransitions.collectAsState(initial = true)
    val hapticsFinish by vm.prefs.hapticsFinish.collectAsState(initial = true)

    fun applyReminder(enabled: Boolean, hour: Int, minute: Int) {
        if (enabled) askNotificationPermission()
        scope.launch { vm.prefs.applyReminder(ctx, enabled, hour, minute) }
    }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ScreenHeader("Settings")

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

        AppearanceCard(
            themeMode = themeMode,
            onThemeModeChange = { scope.launch { vm.prefs.setThemeMode(it) } },
            durationUnit = durationUnit,
            onDurationUnitChange = { scope.launch { vm.prefs.setDurationUnit(it) } },
        )

        HapticsCard(
            transitions = hapticsTransitions,
            onTransitionsChange = { scope.launch { vm.prefs.setHapticsTransitions(it) } },
            finish = hapticsFinish,
            onFinishChange = { scope.launch { vm.prefs.setHapticsFinish(it) } },
        )

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
private fun AppearanceCard(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    durationUnit: DurationUnit,
    onDurationUnitChange: (DurationUnit) -> Unit,
) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionHeader("Appearance", topPadding = 0.dp)
            Text("Theme", style = MaterialTheme.typography.bodyMedium)
            ThemeSegmented(selected = themeMode, onChange = onThemeModeChange)
            Text("Duration display", style = MaterialTheme.typography.bodyMedium)
            DurationSegmented(selected = durationUnit, onChange = onDurationUnitChange)
        }
    }
}

@Composable
private fun ThemeSegmented(selected: ThemeMode, onChange: (ThemeMode) -> Unit) {
    val options = listOf(
        ThemeMode.SYSTEM to "System",
        ThemeMode.LIGHT to "Light",
        ThemeMode.DARK to "Dark",
    )
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { i, (mode, label) ->
            SegmentedButton(
                selected = selected == mode,
                onClick = { onChange(mode) },
                shape = SegmentedButtonDefaults.itemShape(index = i, count = options.size),
                label = { Text(label) },
            )
        }
    }
}

@Composable
private fun DurationSegmented(selected: DurationUnit, onChange: (DurationUnit) -> Unit) {
    val options = listOf(
        DurationUnit.SECONDS to "Seconds",
        DurationUnit.MINUTES_SHORT to "Minutes",
    )
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { i, (unit, label) ->
            SegmentedButton(
                selected = selected == unit,
                onClick = { onChange(unit) },
                shape = SegmentedButtonDefaults.itemShape(index = i, count = options.size),
                label = { Text(label) },
            )
        }
    }
}

@Composable
private fun HapticsCard(
    transitions: Boolean,
    onTransitionsChange: (Boolean) -> Unit,
    finish: Boolean,
    onFinishChange: (Boolean) -> Unit,
) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp)) {
            SectionHeader("Haptics", topPadding = 0.dp)
            HapticsRow(
                title = "Stretch transitions",
                checked = transitions,
                onChange = onTransitionsChange,
            )
            HapticsRow(
                title = "Routine finish",
                checked = finish,
                onChange = onFinishChange,
            )
        }
    }
}

@Composable
private fun HapticsRow(title: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onChange)
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
