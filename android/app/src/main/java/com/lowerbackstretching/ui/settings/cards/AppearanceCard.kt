package com.lowerbackstretching.ui.settings.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.core.DurationUnit
import com.lowerbackstretching.core.ThemeMode
import com.lowerbackstretching.ui.AppViewModel
import com.lowerbackstretching.ui.components.SectionHeader
import kotlinx.coroutines.launch

@Composable
fun AppearanceCard(vm: AppViewModel = viewModel()) {
    val scope = rememberCoroutineScope()
    val themeMode by vm.prefs.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
    val durationUnit by vm.prefs.durationUnit.collectAsState(initial = DurationUnit.SECONDS)

    Card(shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionHeader("Appearance", topPadding = 0.dp)
            Text("Theme", style = MaterialTheme.typography.bodyMedium)
            Segmented(
                options = listOf(
                    ThemeMode.SYSTEM to "System",
                    ThemeMode.LIGHT to "Light",
                    ThemeMode.DARK to "Dark",
                ),
                selected = themeMode,
                onChange = { scope.launch { vm.prefs.setThemeMode(it) } },
            )
            Text("Duration display", style = MaterialTheme.typography.bodyMedium)
            Segmented(
                options = listOf(
                    DurationUnit.SECONDS to "Seconds",
                    DurationUnit.MINUTES_SHORT to "Minutes",
                ),
                selected = durationUnit,
                onChange = { scope.launch { vm.prefs.setDurationUnit(it) } },
            )
        }
    }
}

@Composable
private fun <T> Segmented(
    options: List<Pair<T, String>>,
    selected: T,
    onChange: (T) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { i, (value, label) ->
            SegmentedButton(
                selected = selected == value,
                onClick = { onChange(value) },
                shape = SegmentedButtonDefaults.itemShape(index = i, count = options.size),
                label = { Text(label) },
            )
        }
    }
}
