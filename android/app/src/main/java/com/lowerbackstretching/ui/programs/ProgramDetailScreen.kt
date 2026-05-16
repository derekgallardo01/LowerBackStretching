package com.lowerbackstretching.ui.programs

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.lowerbackstretching.data.model.Program
import com.lowerbackstretching.data.model.ProgramDay
import com.lowerbackstretching.ui.AppViewModel
import com.lowerbackstretching.ui.components.InfoRow

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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
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
            items(program.days, key = { it.day }) { day ->
                InfoRow(
                    title = day.headerTitle(),
                    subtitle = day.subtitle(secondsFor(program, day)),
                    onClick = { onStartDay(day.day) },
                )
            }
        }
    }
}

private fun ProgramDetailViewModel.secondsFor(program: Program, day: ProgramDay): Int =
    content.stretchesFor(program, day.day).sumOf { it.durationSeconds }

internal fun ProgramDay.headerTitle(): String = "Day $day · $title"

internal fun ProgramDay.subtitle(totalSeconds: Int): String =
    "${stretchIds.size} stretches · ${totalSeconds / 60} min"
