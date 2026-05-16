package com.lowerbackstretching.ui.programs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.data.BodyParts.ALL
import com.lowerbackstretching.data.subtitle
import com.lowerbackstretching.ui.AppViewModel
import com.lowerbackstretching.ui.components.ChipsRow
import com.lowerbackstretching.ui.components.InfoRow
import com.lowerbackstretching.ui.components.ScreenHeader
import com.lowerbackstretching.ui.components.SectionHeader

@Composable
fun ProgramsScreen(
    onOpenProgram: (String) -> Unit,
    onOpenCustomRoutine: (Long) -> Unit,
    onCreateRoutine: () -> Unit,
    vm: AppViewModel = viewModel(),
) {
    val programs = vm.content.programs
    val customRoutines by vm.customRoutines.all().collectAsState(initial = emptyList())
    val categories = remember(programs) { listOf(ALL) + programs.map { it.category }.distinct() }
    var selectedCategory by remember { mutableStateOf(ALL) }
    val visiblePrograms = if (selectedCategory == ALL) programs
                          else programs.filter { it.category == selectedCategory }

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
            item { ScreenHeader("Programs") }

            if (customRoutines.isNotEmpty()) {
                item { SectionHeader("My routines", topPadding = 4.dp) }
                items(customRoutines, key = { "custom-${it.id}" }) { routine ->
                    InfoRow(
                        title = routine.name,
                        subtitle = routine.subtitle(vm.content.totalDurationSeconds(routine.stretchIds)),
                        onClick = { onOpenCustomRoutine(routine.id) },
                    )
                }
                item { SectionHeader("Built-in programs", topPadding = 8.dp) }
            }

            item { ChipsRow(options = categories, selected = selectedCategory, onSelect = { selectedCategory = it }) }

            items(visiblePrograms, key = { "prog-${it.id}" }) { program ->
                InfoRow(
                    title = program.title,
                    subtitle = program.subtitle,
                    body = program.summary,
                    onClick = { onOpenProgram(program.id) },
                )
            }
        }
    }
}

