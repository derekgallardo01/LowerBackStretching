package com.lowerbackstretching.ui.programs

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.data.db.CustomRoutineEntity
import com.lowerbackstretching.data.model.Program
import com.lowerbackstretching.ui.AppViewModel

class ProgramsViewModel(app: Application) : AppViewModel(app)

@Composable
fun ProgramsScreen(
    onOpenProgram: (String) -> Unit,
    onOpenCustomRoutine: (Long) -> Unit,
    onCreateRoutine: () -> Unit,
    vm: ProgramsViewModel = viewModel(),
) {
    val programs = vm.content.programs
    val customRoutines by vm.customRoutines.all().collectAsState(initial = emptyList())
    val categories = remember(programs) { listOf("all") + programs.map { it.category }.distinct() }
    var selected by remember { mutableStateOf("all") }
    val visible = if (selected == "all") programs else programs.filter { it.category == selected }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateRoutine,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("New routine") },
            )
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier.padding(inner),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text("Programs", style = MaterialTheme.typography.headlineMedium)
            }

            if (customRoutines.isNotEmpty()) {
                item {
                    Text(
                        "My routines",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                items(customRoutines, key = { "custom-${it.id}" }) { routine ->
                    CustomRoutineCard(
                        routine = routine,
                        stretchCount = routine.stretchIds.size,
                        durationSeconds = routine.stretchIds.sumOf {
                            vm.content.stretch(it)?.durationSeconds ?: 0
                        },
                        onClick = { onOpenCustomRoutine(routine.id) },
                    )
                }
                item {
                    Text(
                        "Built-in programs",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selected == cat,
                            onClick = { selected = cat },
                            label = { Text(cat.replace('-', ' ')) },
                        )
                    }
                }
            }

            items(visible, key = { "prog-${it.id}" }) { program ->
                ProgramRow(program = program, onClick = { onOpenProgram(program.id) })
            }
        }
    }
}

@Composable
private fun ProgramRow(program: Program, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(program.title, style = MaterialTheme.typography.titleLarge)
            Text(
                "${program.days.size}-day · ${program.category.replace('-', ' ')}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                program.summary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

@Composable
private fun CustomRoutineCard(
    routine: CustomRoutineEntity,
    stretchCount: Int,
    durationSeconds: Int,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.padding(end = 8.dp)) {
                Text(routine.name, style = MaterialTheme.typography.titleLarge)
                Text(
                    "$stretchCount stretches · ${durationSeconds / 60} min",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
