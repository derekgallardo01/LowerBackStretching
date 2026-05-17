package com.lowerbackstretching.ui.home

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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.data.subtitle
import com.lowerbackstretching.data.xpForSession
import com.lowerbackstretching.data.xpProgress
import com.lowerbackstretching.ui.AppViewModel
import com.lowerbackstretching.ui.components.InfoRow
import com.lowerbackstretching.ui.components.ScreenHeader
import com.lowerbackstretching.ui.components.SectionHeader
import com.lowerbackstretching.ui.components.Stat


@Composable
fun HomeScreen(
    onOpenProgram: (String) -> Unit,
    onOpenAchievements: () -> Unit,
    onOpenGoals: () -> Unit,
    vm: AppViewModel = viewModel(),
) {
    val streak by vm.sessions.streak().collectAsState(initial = 0)
    val total by vm.sessions.count().collectAsState(initial = 0)
    val totalSeconds by vm.sessions.totalDurationSeconds().collectAsState(initial = 0)
    val xp = remember(totalSeconds) { xpProgress(xpForSession(totalSeconds)) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { ScreenHeader("Welcome back") }
        item { StatsCard(streak = streak, total = total, level = xp.level, xpProgress = xp.progress, xpIntoLevel = xp.xpIntoLevel, xpToNextLevel = xp.xpToNextLevel) }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                QuickCard(
                    modifier = Modifier.weight(1f),
                    title = "Goals",
                    body = "Weekly & monthly targets",
                    onClick = onOpenGoals,
                )
                QuickCard(
                    modifier = Modifier.weight(1f),
                    title = "Achievements",
                    body = "Badges & milestones",
                    onClick = onOpenAchievements,
                )
            }
        }
        item { SectionHeader("Programs") }
        items(vm.content.programs, key = { it.id }) { program ->
            InfoRow(
                title = program.title,
                subtitle = program.subtitle,
                body = program.summary,
                onClick = { onOpenProgram(program.id) },
            )
        }
    }
}

@Composable
private fun StatsCard(
    streak: Int,
    total: Int,
    level: Int,
    xpProgress: Float,
    xpIntoLevel: Int,
    xpToNextLevel: Int,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Stat(value = "$streak", label = "Day streak")
                Stat(value = "$total", label = "Sessions")
                Stat(value = "L$level", label = "Level")
            }
            LinearProgressIndicator(
                progress = { xpProgress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                "$xpIntoLevel / $xpToNextLevel XP to next level",
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun QuickCard(
    title: String,
    body: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), onClick = onClick) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
        }
    }
}
