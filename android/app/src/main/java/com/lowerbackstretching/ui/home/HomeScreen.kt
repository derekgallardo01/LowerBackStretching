package com.lowerbackstretching.ui.home

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.lowerbackstretching.data.model.Program
import com.lowerbackstretching.ui.AppViewModel

class HomeViewModel(app: Application) : AppViewModel(app)

@Composable
fun HomeScreen(
    onOpenPrograms: () -> Unit,
    onOpenProgram: (String) -> Unit,
    vm: HomeViewModel = viewModel(),
) {
    val streak by vm.sessions.streak().collectAsState(initial = 0)
    val total by vm.sessions.count().collectAsState(initial = 0)
    val programs = vm.content.programs

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Welcome back", style = MaterialTheme.typography.headlineMedium)
        }
        item {
            StreakCard(streak = streak, total = total)
        }
        item {
            Text("Programs", style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp))
        }
        items(programs, key = { it.id }) { program ->
            ProgramCard(program = program, onClick = { onOpenProgram(program.id) })
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

@Composable
private fun Stat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineLarge)
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun ProgramCard(program: Program, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(program.title, style = MaterialTheme.typography.titleLarge)
            Text("${program.days.size}-day · ${program.category}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary)
            Text(program.summary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 6.dp))
        }
    }
}
