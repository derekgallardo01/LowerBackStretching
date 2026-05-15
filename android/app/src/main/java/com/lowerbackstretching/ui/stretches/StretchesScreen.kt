package com.lowerbackstretching.ui.stretches

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.lowerbackstretching.data.model.Stretch
import com.lowerbackstretching.ui.AppViewModel

class StretchesViewModel(app: Application) : AppViewModel(app)

@Composable
fun StretchesScreen(
    onOpenStretch: (String) -> Unit,
    vm: StretchesViewModel = viewModel(),
) {
    val all = vm.content.stretches
    val bodyParts = remember(all) {
        listOf("all") + all.flatMap { it.bodyParts }.distinct().sorted()
    }
    var selected by remember { mutableStateOf("all") }
    val visible = if (selected == "all") all else all.filter { selected in it.bodyParts }

    Column {
        Text(
            "Stretches",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(bodyParts) { part ->
                FilterChip(
                    selected = selected == part,
                    onClick = { selected = part },
                    label = { Text(part.replace('-', ' ')) },
                )
            }
        }
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(visible, key = { it.id }) { stretch ->
                StretchRow(stretch = stretch, onClick = { onOpenStretch(stretch.id) })
            }
        }
    }
}

@Composable
private fun StretchRow(stretch: Stretch, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(stretch.name, style = MaterialTheme.typography.titleMedium)
            Text(
                "${stretch.durationSeconds}s · ${stretch.difficulty} · ${stretch.bodyParts.joinToString(" · ") { it.replace('-', ' ') }}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
