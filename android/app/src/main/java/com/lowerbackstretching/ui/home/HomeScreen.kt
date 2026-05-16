package com.lowerbackstretching.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.ui.AppViewModel
import com.lowerbackstretching.ui.components.InfoRow
import com.lowerbackstretching.ui.components.Stat
import com.lowerbackstretching.ui.programs.subtitle


@Composable
fun HomeScreen(
    onOpenProgram: (String) -> Unit,
    vm: AppViewModel = viewModel(),
) {
    val streak by vm.sessions.streak().collectAsState(initial = 0)
    val total by vm.sessions.count().collectAsState(initial = 0)

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text("Welcome back", style = MaterialTheme.typography.headlineMedium) }
        item { StreakCard(streak = streak, total = total) }
        item {
            Text(
                "Programs",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        items(vm.content.programs, key = { it.id }) { program ->
            InfoRow(
                title = program.title,
                subtitle = program.subtitle(),
                body = program.summary,
                onClick = { onOpenProgram(program.id) },
            )
        }
    }
}

@Composable
private fun StreakCard(streak: Int, total: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Stat(value = "$streak", label = "Day streak")
            Stat(value = "$total", label = "Sessions")
        }
    }
}
