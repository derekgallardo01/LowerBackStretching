package com.lowerbackstretching.ui.share

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.core.SharedRoutine
import com.lowerbackstretching.ui.AppViewModel
import kotlinx.coroutines.launch

/**
 * Shown when a deep link with a shared routine fires the app. Lets the
 * user preview the routine and decide whether to add it to their
 * library. Unknown stretch ids in the import are dropped on save.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportRoutineSheet(
    routine: SharedRoutine,
    onDismiss: () -> Unit,
    vm: AppViewModel = viewModel(),
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    val knownIds = routine.stretchIds.filter { vm.content.stretch(it) != null }
    val unknownCount = routine.stretchIds.size - knownIds.size

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Import routine", style = MaterialTheme.typography.titleLarge)
            Text(routine.name, style = MaterialTheme.typography.titleMedium)
            Text(
                "${knownIds.size} stretches" + if (unknownCount > 0)
                    " (skipped $unknownCount unknown id${if (unknownCount > 1) "s" else ""})"
                else "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
            knownIds.mapNotNull { vm.content.stretch(it)?.name }.forEach { name ->
                Text("• $name", style = MaterialTheme.typography.bodyMedium)
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        scope.launch {
                            vm.customRoutines.create(routine.name, knownIds)
                            onDismiss()
                        }
                    },
                    enabled = knownIds.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Add to my routines")
                }
            }
        }
    }
}
