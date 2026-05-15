package com.lowerbackstretching.ui.programs

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.data.model.ProgramDay
import com.lowerbackstretching.ui.AppViewModel

class ProgramDetailViewModel(app: Application) : AppViewModel(app)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramDetailScreen(
    programId: String,
    onStartDay: (Int) -> Unit,
    onBack: () -> Unit,
    vm: ProgramDetailViewModel = viewModel(),
) {
    val program = vm.content.program(programId) ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(program.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
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
            item {
                Text(program.summary, style = MaterialTheme.typography.bodyLarge)
            }
            items(program.days, key = { it.day }) { day ->
                DayRow(
                    day = day,
                    durationSeconds = vm.content.stretchesFor(program, day.day).sumOf { it.durationSeconds },
                    onStart = { onStartDay(day.day) },
                )
            }
        }
    }
}

@Composable
private fun DayRow(
    day: ProgramDay,
    durationSeconds: Int,
    onStart: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onStart,
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Day ${day.day} · ${day.title}", style = MaterialTheme.typography.titleMedium)
            Text("${day.stretchIds.size} stretches · ${durationSeconds / 60} min",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary)
            Row(
                Modifier.padding(top = 8.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Start")
            }
        }
    }
}

