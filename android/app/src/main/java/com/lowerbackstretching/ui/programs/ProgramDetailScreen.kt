package com.lowerbackstretching.ui.programs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.core.headerTitle
import com.lowerbackstretching.core.subtitle
import com.lowerbackstretching.ui.AppViewModel
import com.lowerbackstretching.ui.components.InfoRow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramDetailScreen(
    programId: String,
    onStartDay: (Int) -> Unit,
    onBack: () -> Unit,
    vm: AppViewModel = viewModel(),
) {
    val program = vm.content.program(programId) ?: return
    val totalDays = program.days.size
    val currentDay by vm.programProgress.currentDay(programId).collectAsState(initial = 1)
    val scope = rememberCoroutineScope()
    val completedProgram = currentDay > totalDays

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(program.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier.padding(inner),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item { Text(program.summary, style = MaterialTheme.typography.bodyLarge) }

            item {
                ProgressCallout(
                    currentDay = currentDay,
                    totalDays = totalDays,
                    completed = completedProgram,
                    onResume = { onStartDay(currentDay.coerceAtMost(totalDays)) },
                    onReset = { scope.launch { vm.programProgress.reset(programId) } },
                )
            }

            items(program.days, key = { it.day }) { day ->
                InfoRow(
                    title = day.headerTitle + if (day.day == currentDay && !completedProgram) " · Today" else "",
                    subtitle = day.subtitle(vm.content.totalDurationSeconds(day.stretchIds)),
                    onClick = { onStartDay(day.day) },
                )
            }
        }
    }
}

@Composable
private fun ProgressCallout(
    currentDay: Int,
    totalDays: Int,
    completed: Boolean,
    onResume: () -> Unit,
    onReset: () -> Unit,
) {
    when {
        completed -> Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "Program complete.",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(end = 4.dp),
            )
            TextButton(onClick = onReset) {
                Icon(Icons.Filled.Restore, contentDescription = null)
                Text(" Restart")
            }
        }
        currentDay > 1 -> Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(onClick = onResume) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Text(" Resume Day $currentDay")
            }
            TextButton(onClick = onReset) {
                Text("Start over")
            }
        }
        else -> Button(onClick = onResume) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null)
            Text(" Start Day 1 of $totalDays")
        }
    }
}
