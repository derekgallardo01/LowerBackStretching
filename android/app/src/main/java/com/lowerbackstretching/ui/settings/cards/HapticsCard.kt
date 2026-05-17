package com.lowerbackstretching.ui.settings.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    SettingsCard(verticalSpacing = 0.dp) {
        SectionHeader("Haptics", topPadding = 0.dp)
        HapticsToggleRow(
            title = "Stretch transitions",
            checked = transitions,
            onChange = { scope.launch { vm.prefs.setHapticsTransitions(it) } },
        )
        HapticsToggleRow(
            title = "Routine finish",
            checked = finish,
            onChange = { scope.launch { vm.prefs.setHapticsFinish(it) } },
        )
    }
}

@Composable
private fun HapticsToggleRow(title: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
