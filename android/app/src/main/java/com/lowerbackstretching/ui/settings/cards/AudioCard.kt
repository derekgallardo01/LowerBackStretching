package com.lowerbackstretching.ui.settings.cards

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.core.AmbientTrack
import com.lowerbackstretching.core.ChimeTrack
import com.lowerbackstretching.core.MusicTrack
import com.lowerbackstretching.ui.AppViewModel
import com.lowerbackstretching.ui.components.SectionHeader
import kotlinx.coroutines.launch

@Composable
fun AudioCard(vm: AppViewModel = viewModel()) {
    val scope = rememberCoroutineScope()
    val music by vm.prefs.musicTrack.collectAsState(initial = MusicTrack.NONE)
    val musicVolume by vm.prefs.musicVolume.collectAsState(initial = 0.4f)
    val ambient by vm.prefs.ambientTrack.collectAsState(initial = AmbientTrack.NONE)
    val ambientVolume by vm.prefs.ambientVolume.collectAsState(initial = 0.6f)
    val chime by vm.prefs.chimeTrack.collectAsState(initial = ChimeTrack.NONE)

    SettingsCard {
        SectionHeader("Audio", topPadding = 0.dp)

        TrackDropdown(
            label = "Music",
            options = MusicTrack.entries.map { it to it.displayName },
            selected = music,
            onChange = { scope.launch { vm.prefs.setMusicTrack(it) } },
        )
        VolumeSlider(
            label = "Music volume",
            value = musicVolume,
            onValueChange = { scope.launch { vm.prefs.setMusicVolume(it) } },
        )

        TrackDropdown(
            label = "Ambient",
            options = AmbientTrack.entries.map { it to it.displayName },
            selected = ambient,
            onChange = { scope.launch { vm.prefs.setAmbientTrack(it) } },
        )
        VolumeSlider(
            label = "Ambient volume",
            value = ambientVolume,
            onValueChange = { scope.launch { vm.prefs.setAmbientVolume(it) } },
        )

        TrackDropdown(
            label = "Chime on transition",
            options = ChimeTrack.entries.map { it to it.displayName },
            selected = chime,
            onChange = { scope.launch { vm.prefs.setChimeTrack(it) } },
        )
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
