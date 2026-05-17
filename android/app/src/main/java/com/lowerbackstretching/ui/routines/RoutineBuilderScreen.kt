package com.lowerbackstretching.ui.routines

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.core.BodyParts
import com.lowerbackstretching.core.DurationUnit
import com.lowerbackstretching.core.filteredBy
import com.lowerbackstretching.core.shortSubtitle
import com.lowerbackstretching.ui.AppViewModel
import com.lowerbackstretching.ui.components.ChipsRow
import com.lowerbackstretching.ui.components.InfoRow
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineBuilderScreen(
    onSaved: () -> Unit,
    onBack: () -> Unit,
    vm: AppViewModel = viewModel(),
) {
    val scope = rememberCoroutineScope()
    val stretches = vm.content.stretches
    val filterOptions = remember(stretches) { BodyParts.filterOptions(stretches) }

    var name by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(BodyParts.ALL) }
    val selected = remember { mutableStateListOf<String>() }

    val canSave = name.trim().isNotEmpty() && selected.isNotEmpty()
    val totalSeconds = vm.content.totalDurationSeconds(selected)
    val unit by vm.prefs.durationUnit.collectAsState(initial = DurationUnit.SECONDS)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New routine") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                    ) { Icon(Icons.Filled.Check, contentDescription = "Save") }
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
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp),
            )
            Text(
                "${selected.size} stretches selected · ${totalSeconds / 60} min",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            ChipsRow(options = filterOptions, selected = filter, onSelect = { filter = it })

            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp, top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(stretches.filteredBy(filter), key = { it.id }) { stretch ->
                    val isSelected = stretch.id in selected
                    InfoRow(
                        title = stretch.name,
                        subtitle = stretch.shortSubtitle(unit),
                        onClick = {
                            if (isSelected) selected.remove(stretch.id) else selected.add(stretch.id)
                        },
                        trailing = if (isSelected) {
                            { Icon(Icons.Filled.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary) }
                        } else null,
                    )
                }
            }
        }
    }
}
