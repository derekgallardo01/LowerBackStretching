package com.lowerbackstretching.ui.routines

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.data.model.Stretch
import com.lowerbackstretching.ui.AppViewModel
import kotlinx.coroutines.launch

class RoutineBuilderViewModel(app: Application) : AppViewModel(app)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineBuilderScreen(
    onSaved: () -> Unit,
    onBack: () -> Unit,
    vm: RoutineBuilderViewModel = viewModel(),
) {
    val scope = rememberCoroutineScope()
    val stretches = vm.content.stretches
    val bodyParts = remember(stretches) {
        listOf("all") + stretches.flatMap { it.bodyParts }.distinct().sorted()
    }

    var name by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("all") }
    val selected = remember { mutableStateListOf<String>() }

    val canSave = name.trim().isNotEmpty() && selected.isNotEmpty()
    val visible = if (filter == "all") stretches else stretches.filter { filter in it.bodyParts }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New routine") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(
                        enabled = canSave,
                        onClick = {
                            scope.launch {
                                vm.customRoutines.create(name = name, stretchIds = selected.toList())
                                onSaved()
                            }
                        },
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Save")
                    }
                },
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier.padding(inner).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Routine name") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            )

            Text(
                "${selected.size} stretches selected · ${(selected.sumOf { vm.content.stretch(it)?.durationSeconds ?: 0 }) / 60} min",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(bodyParts) { part ->
                    FilterChip(
                        selected = filter == part,
                        onClick = { filter = part },
                        label = { Text(part.replace('-', ' ')) },
                    )
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp, top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(visible, key = { it.id }) { stretch ->
                    PickerRow(
                        stretch = stretch,
                        isSelected = stretch.id in selected,
                        onToggle = {
                            if (stretch.id in selected) selected.remove(stretch.id)
                            else selected.add(stretch.id)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun PickerRow(stretch: Stretch, isSelected: Boolean, onToggle: () -> Unit) {
    Card(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.padding(end = 8.dp)) {
                Text(stretch.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${stretch.durationSeconds}s · ${stretch.bodyParts.joinToString(" · ") { it.replace('-', ' ') }}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (isSelected) {
                Icon(Icons.Filled.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
