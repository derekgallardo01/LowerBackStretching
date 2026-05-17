package com.lowerbackstretching.ui.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.core.monthlyCompletions
import com.lowerbackstretching.core.weeklyCompletions
import com.lowerbackstretching.ui.AppViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(onBack: () -> Unit, vm: AppViewModel = viewModel()) {
    val scope = rememberCoroutineScope()
    val completedDays by vm.sessions.completedDays().collectAsState(initial = emptySet())
    val weeklyGoal by vm.prefs.weeklyGoal.collectAsState(initial = 3)
    val monthlyGoal by vm.prefs.monthlyGoal.collectAsState(initial = 12)

    val weekly = remember(completedDays) { weeklyCompletions(completedDays) }
    val monthly = remember(completedDays) { monthlyCompletions(completedDays) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Goals") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier.padding(inner),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                GoalCard(
                    title = "This week",
                    completed = weekly,
                    target = weeklyGoal,
                    sliderRange = 1f..14f,
                    onTargetChange = { scope.launch { vm.prefs.setWeeklyGoal(it) } },
                )
            }
            item {
                GoalCard(
                    title = "This month",
                    completed = monthly,
                    target = monthlyGoal,
                    sliderRange = 1f..30f,
                    onTargetChange = { scope.launch { vm.prefs.setMonthlyGoal(it) } },
                )
            }
        }
    }
}

@Composable
private fun GoalCard(
    title: String,
    completed: Int,
    target: Int,
    sliderRange: ClosedFloatingPointRange<Float>,
    onTargetChange: (Int) -> Unit,
) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                "$completed of $target days",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            LinearProgressIndicator(
                progress = {
                    if (target == 0) 0f else (completed.toFloat() / target).coerceIn(0f, 1f)
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                "Target: $target",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
            Slider(
                value = target.toFloat(),
                onValueChange = { onTargetChange(it.toInt()) },
                valueRange = sliderRange,
                steps = (sliderRange.endInclusive - sliderRange.start).toInt() - 1,
                modifier = Modifier.semantics { stateDescription = "Target $target days" },
            )
        }
    }
}
