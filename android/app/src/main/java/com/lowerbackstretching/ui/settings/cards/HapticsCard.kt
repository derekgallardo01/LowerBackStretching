package com.lowerbackstretching.ui.settings.cards

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.ui.AppViewModel
import com.lowerbackstretching.ui.components.SectionHeader
import kotlinx.coroutines.launch

@Composable
fun HapticsCard(vm: AppViewModel = viewModel()) {
    val scope = rememberCoroutineScope()
    val transitions by vm.prefs.hapticsTransitions.collectAsState(initial = true)
    val finish by vm.prefs.hapticsFinish.collectAsState(initial = true)

    SettingsCard(verticalSpacing = 8.dp) {
        SectionHeader("Haptics", topPadding = 0.dp)
        SettingsToggleRow(
            checked = transitions,
            onChange = { scope.launch { vm.prefs.setHapticsTransitions(it) } },
        ) {
            Text("Stretch transitions", style = MaterialTheme.typography.bodyMedium)
        }
        SettingsToggleRow(
            checked = finish,
            onChange = { scope.launch { vm.prefs.setHapticsFinish(it) } },
        ) {
            Text("Routine finish", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
