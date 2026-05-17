package com.lowerbackstretching.ui.settings

import android.app.TimePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.audio.AmbientTrack
import com.lowerbackstretching.audio.ChimeTrack
import com.lowerbackstretching.audio.MusicTrack
import com.lowerbackstretching.data.DurationUnit
import com.lowerbackstretching.data.ThemeMode
import com.lowerbackstretching.health.HealthController
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
    val musicTrack by vm.prefs.musicTrack.collectAsState(initial = MusicTrack.NONE)
    val musicVolume by vm.prefs.musicVolume.collectAsState(initial = 0.4f)
    val ambientTrack by vm.prefs.ambientTrack.collectAsState(initial = AmbientTrack.NONE)
    val ambientVolume by vm.prefs.ambientVolume.collectAsState(initial = 0.6f)
    val chimeTrack by vm.prefs.chimeTrack.collectAsState(initial = ChimeTrack.NONE)
    val healthWrite by vm.prefs.healthWriteEnabled.collectAsState(initial = false)
    val healthRead by vm.prefs.healthReadEnabled.collectAsState(initial = false)
    val healthAvailability = remember { vm.health.availability() }
    val healthLauncher = rememberLauncherForActivityResult(
        contract = vm.health.permissionsContract()
    ) { /* result is the granted Set<String>; flow re-emits the toggle state regardless. */ }
    val cloudSyncEnabled by vm.prefs.cloudSyncEnabled.collectAsState(initial = false)
    val syncBackendType = remember { vm.sync.backendType }
    val syncHasRealBackend = remember { vm.sync.hasRealBackend }

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

        AudioCard(
            music = musicTrack,
            onMusicChange = { scope.launch { vm.prefs.setMusicTrack(it) } },
            musicVolume = musicVolume,
            onMusicVolumeChange = { scope.launch { vm.prefs.setMusicVolume(it) } },
            ambient = ambientTrack,
            onAmbientChange = { scope.launch { vm.prefs.setAmbientTrack(it) } },
            ambientVolume = ambientVolume,
            onAmbientVolumeChange = { scope.launch { vm.prefs.setAmbientVolume(it) } },
            chime = chimeTrack,
            onChimeChange = { scope.launch { vm.prefs.setChimeTrack(it) } },
        )

        HealthCard(
            availability = healthAvailability,
            write = healthWrite,
            onWriteChange = { on ->
                scope.launch { vm.prefs.setHealthWriteEnabled(on) }
                if (on) healthLauncher.launch(HealthController.allPermissions)
            },
            read = healthRead,
            onReadChange = { on ->
                scope.launch { vm.prefs.setHealthReadEnabled(on) }
                if (on) healthLauncher.launch(HealthController.allPermissions)
            },
        )

        CloudSyncCard(
            enabled = cloudSyncEnabled,
            backendType = syncBackendType,
            hasRealBackend = syncHasRealBackend,
            onEnabledChange = { on -> scope.launch { vm.sync.setEnabled(on) } },
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
private fun AudioCard(
    music: MusicTrack,
    onMusicChange: (MusicTrack) -> Unit,
    musicVolume: Float,
    onMusicVolumeChange: (Float) -> Unit,
    ambient: AmbientTrack,
    onAmbientChange: (AmbientTrack) -> Unit,
    ambientVolume: Float,
    onAmbientVolumeChange: (Float) -> Unit,
    chime: ChimeTrack,
    onChimeChange: (ChimeTrack) -> Unit,
) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionHeader("Audio", topPadding = 0.dp)

            TrackDropdown(
                label = "Music",
                options = MusicTrack.entries.map { it to it.displayName },
                selected = music,
                onChange = onMusicChange,
            )
            VolumeSlider(
                label = "Music volume",
                value = musicVolume,
                onValueChange = onMusicVolumeChange,
            )

            TrackDropdown(
                label = "Ambient",
                options = AmbientTrack.entries.map { it to it.displayName },
                selected = ambient,
                onChange = onAmbientChange,
            )
            VolumeSlider(
                label = "Ambient volume",
                value = ambientVolume,
                onValueChange = onAmbientVolumeChange,
            )

            TrackDropdown(
                label = "Chime on transition",
                options = ChimeTrack.entries.map { it to it.displayName },
                selected = chime,
                onChange = onChimeChange,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> TrackDropdown(
    label: String,
    options: List<Pair<T, String>>,
    selected: T,
    onChange: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selected }?.second ?: ""
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (value, optionLabel) ->
                DropdownMenuItem(
                    text = { Text(optionLabel) },
                    onClick = { onChange(value); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun VolumeSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Slider(value = value, onValueChange = onValueChange, valueRange = 0f..1f)
    }
}

@Composable
private fun HealthCard(
    availability: HealthController.Availability,
    write: Boolean,
    onWriteChange: (Boolean) -> Unit,
    read: Boolean,
    onReadChange: (Boolean) -> Unit,
) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionHeader("Health Connect", topPadding = 0.dp)
            when (availability) {
                HealthController.Availability.NotInstalled -> Text(
                    "Install the Health Connect app to share stretching sessions with your other fitness apps.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
                HealthController.Availability.ProviderUpdateRequired -> Text(
                    "Health Connect needs an update. Open the Play Store to install the latest version.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
                HealthController.Availability.Available -> {
                    HealthToggleRow(
                        title = "Write stretching sessions",
                        subtitle = "Adds each completed session to Health Connect.",
                        checked = write,
                        onChange = onWriteChange,
                    )
                    HealthToggleRow(
                        title = "Read daily steps",
                        subtitle = "Suggests a cooldown stretch after long walks.",
                        checked = read,
                        onChange = onReadChange,
                    )
                }
            }
        }
    }
}

@Composable
private fun HealthToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.padding(end = 12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun CloudSyncCard(
    enabled: Boolean,
    backendType: String,
    hasRealBackend: Boolean,
    onEnabledChange: (Boolean) -> Unit,
) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionHeader("Cloud sync", topPadding = 0.dp)
            if (!hasRealBackend) {
                Text(
                    "Cloud sync is wired in but the Firebase backend isn't connected yet. " +
                        "Set up the Firebase project (see firebase/README.md) and swap " +
                        "App.syncBackend to FirebaseSyncBackend to enable.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.padding(end = 12.dp)) {
                        Text("Enable cloud sync", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Sessions, routines, and progress back up to your account.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        )
                    }
                    Switch(checked = enabled, onCheckedChange = onEnabledChange)
                }
            }
            Text(
                "Backend: $backendType",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            )
        }
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
