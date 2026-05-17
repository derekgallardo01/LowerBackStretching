package com.lowerbackstretching.ui.settings.cards

import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.health.HealthController
import com.lowerbackstretching.ui.AppViewModel
import com.lowerbackstretching.ui.components.SectionHeader
import kotlinx.coroutines.launch

@Composable
fun HealthCard(vm: AppViewModel = viewModel()) {
    val scope = rememberCoroutineScope()
    val write by vm.prefs.healthWriteEnabled.collectAsState(initial = false)
    val read by vm.prefs.healthReadEnabled.collectAsState(initial = false)
    val availability = remember { vm.health.availability() }
    val launcher = rememberLauncherForActivityResult(
        contract = vm.health.permissionsContract()
    ) { /* result is the granted Set<String>; the flow re-emits regardless. */ }

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
                    ToggleRow(
                        title = "Write stretching sessions",
                        subtitle = "Adds each completed session to Health Connect.",
                        checked = write,
                        onChange = { on ->
                            scope.launch { vm.prefs.setHealthWriteEnabled(on) }
                            if (on) launcher.launch(HealthController.allPermissions)
                        },
                    )
                    ToggleRow(
                        title = "Read daily steps",
                        subtitle = "Suggests a cooldown stretch after long walks.",
                        checked = read,
                        onChange = { on ->
                            scope.launch { vm.prefs.setHealthReadEnabled(on) }
                            if (on) launcher.launch(HealthController.allPermissions)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(
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
