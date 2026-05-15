package com.lowerbackstretching.ui.programs

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.data.model.Program
import com.lowerbackstretching.ui.AppViewModel

class ProgramsViewModel(app: Application) : AppViewModel(app)

@Composable
fun ProgramsScreen(
    onOpenProgram: (String) -> Unit,
    vm: ProgramsViewModel = viewModel(),
) {
    val programs = vm.content.programs
    val categories = remember(programs) { listOf("all") + programs.map { it.category }.distinct() }
    var selected by remember { mutableStateOf("all") }
    val visible = if (selected == "all") programs else programs.filter { it.category == selected }

    Column {
        Text(
            "Programs",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(categories) { cat ->
                FilterChip(
                    selected = selected == cat,
                    onClick = { selected = cat },
                    label = { Text(cat.replace('-', ' ')) },
                )
            }
        }
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(visible, key = { it.id }) { program ->
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
