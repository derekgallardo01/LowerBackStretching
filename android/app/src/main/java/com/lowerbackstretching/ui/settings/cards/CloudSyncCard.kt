package com.lowerbackstretching.ui.settings.cards

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.ui.AppViewModel
import com.lowerbackstretching.ui.components.SectionHeader
import kotlinx.coroutines.launch

@Composable
fun CloudSyncCard(vm: AppViewModel = viewModel()) {
    val scope = rememberCoroutineScope()
    val enabled by vm.prefs.cloudSyncEnabled.collectAsState(initial = false)
    val backendType = remember { vm.sync.backendType }
    val hasRealBackend = remember { vm.sync.hasRealBackend }

    SettingsCard(verticalSpacing = 8.dp) {
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
            SettingsToggleRow(
                checked = enabled,
                onChange = { on -> scope.launch { vm.sync.setEnabled(on) } },
            ) {
                Text("Enable cloud sync", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Sessions, routines, and progress back up to your account.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
            }
        }
        Text(
            "Backend: $backendType",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
        )
    }
}
