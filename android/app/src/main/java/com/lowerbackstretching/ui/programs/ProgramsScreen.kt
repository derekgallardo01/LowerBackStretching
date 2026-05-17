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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.data.BodyParts.ALL
import com.lowerbackstretching.data.db.CustomRoutineEntity
import com.lowerbackstretching.data.subtitle
import com.lowerbackstretching.ui.AppViewModel
import com.lowerbackstretching.ui.components.ChipsRow
import com.lowerbackstretching.ui.components.InfoRow
import com.lowerbackstretching.ui.components.ScreenHeader
import com.lowerbackstretching.ui.components.SectionHeader
import kotlinx.coroutines.launch

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

    val scope = rememberCoroutineScope()
    val snackbarHost = remember { SnackbarHostState() }
    var actionsTarget by remember { mutableStateOf<CustomRoutineEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateRoutine,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("New routine") },
                modifier = Modifier.semantics { contentDescription = "New routine" },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) },
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
                        onLongClick = { actionsTarget = routine },
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

    actionsTarget?.let { target ->
        val index = customRoutines.indexOfFirst { it.id == target.id }
        RoutineActionsSheet(
            routineName = target.name,
            canMoveUp = index > 0,
            canMoveDown = index >= 0 && index < customRoutines.size - 1,
            onDuplicate = {
                scope.launch { vm.customRoutines.duplicate(target) }
                actionsTarget = null
            },
            onMoveUp = {
                if (index > 0) {
                    val reordered = customRoutines.toMutableList().apply {
                        val moving = removeAt(index)
                        add(index - 1, moving)
                    }
                    scope.launch { vm.customRoutines.reorder(reordered.map { it.id }) }
                }
                actionsTarget = null
            },
            onMoveDown = {
                if (index in 0..(customRoutines.size - 2)) {
                    val reordered = customRoutines.toMutableList().apply {
                        val moving = removeAt(index)
                        add(index + 1, moving)
                    }
                    scope.launch { vm.customRoutines.reorder(reordered.map { it.id }) }
                }
                actionsTarget = null
            },
            onDelete = {
                actionsTarget = null
                scope.launch {
                    vm.customRoutines.softDelete(target)
                    val result = snackbarHost.showSnackbar(
                        message = "Deleted ${target.name}",
                        actionLabel = "Undo",
                        withDismissAction = true,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        vm.customRoutines.restore(target)
                    }
                }
            },
            onDismiss = { actionsTarget = null },
        )
    }
}
