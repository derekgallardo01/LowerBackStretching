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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.core.subtitle
import com.lowerbackstretching.core.xpForSession
import com.lowerbackstretching.core.xpProgress
import com.lowerbackstretching.health.shouldShowCooldown
import com.lowerbackstretching.ui.AppViewModel
import com.lowerbackstretching.ui.components.InfoRow
import com.lowerbackstretching.ui.components.ScreenHeader
import com.lowerbackstretching.ui.components.SectionHeader
import com.lowerbackstretching.ui.components.Stat
import java.time.LocalDate

/**
 * Every navigation / system-intent the Home screen can trigger.
 * AppNav owns the routing; HomeScreen just emits the intent.
 */
sealed interface HomeAction {
    data class OpenProgram(val id: String) : HomeAction
    data object OpenAchievements : HomeAction
    data object OpenGoals : HomeAction
    data object OpenFlexibility : HomeAction
    data object OpenGlossary : HomeAction
    data object OpenBodyDiagram : HomeAction
    data object ScheduleBreak : HomeAction
}

@Composable
fun HomeScreen(
    onAction: (HomeAction) -> Unit,
    vm: AppViewModel = viewModel(),
) {
    val streak by vm.sessions.streak().collectAsState(initial = 0)
    val total by vm.sessions.count().collectAsState(initial = 0)
    val totalSeconds by vm.sessions.totalDurationSeconds().collectAsState(initial = 0)
    val xp = remember(totalSeconds) { xpProgress(xpForSession(totalSeconds)) }

    val healthReadEnabled by vm.prefs.healthReadEnabled.collectAsState(initial = false)
    val lastSessionDay by vm.prefs.lastSessionEpochDay.collectAsState(initial = 0L)
    val stretchedToday = lastSessionDay == LocalDate.now().toEpochDay()
    var stepsToday by remember { mutableStateOf<Long?>(null) }
    LaunchedEffect(healthReadEnabled) {
        stepsToday = if (healthReadEnabled) vm.health.readStepsToday() else null
    }
    val showCooldown = shouldShowCooldown(healthReadEnabled, stretchedToday, stepsToday)

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { ScreenHeader("Welcome back") }
        if (showCooldown) {
            item {
                CooldownCard(
                    steps = stepsToday ?: 0L,
                    onAction = {
                        vm.content.programs.firstOrNull()?.let {
                            onAction(HomeAction.OpenProgram(it.id))
                        }
                    },
                )
            }
        }
        item {
            StatsCard(
                streak = streak,
                total = total,
                level = xp.level,
                xpProgress = xp.progress,
                xpIntoLevel = xp.xpIntoLevel,
                xpToNextLevel = xp.xpToNextLevel,
            )
        }
        items(quickActionRows) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                row.forEach { quick ->
                    QuickCard(
                        modifier = Modifier.weight(1f),
                        title = quick.title,
                        body = quick.subtitle,
                        onClick = { onAction(quick.action) },
                    )
                }
            }
        }
        item { SectionHeader("Programs") }
        items(vm.content.programs, key = { it.id }) { program ->
            InfoRow(
                title = program.title,
                subtitle = program.subtitle,
                body = program.summary,
                onClick = { onAction(HomeAction.OpenProgram(program.id)) },
            )
        }
    }
}

private data class QuickActionSpec(
    val title: String,
    val subtitle: String,
    val action: HomeAction,
)

private val quickActionRows: List<List<QuickActionSpec>> = listOf(
    listOf(
        QuickActionSpec("Goals", "Weekly & monthly targets", HomeAction.OpenGoals),
        QuickActionSpec("Achievements", "Badges & milestones", HomeAction.OpenAchievements),
    ),
    listOf(
        QuickActionSpec("Flexibility self-test", "Track your reach over time", HomeAction.OpenFlexibility),
        QuickActionSpec("Glossary", "Anatomy & stretching terms", HomeAction.OpenGlossary),
    ),
    listOf(
        QuickActionSpec("Tap where it hurts", "Find a stretch by body area", HomeAction.OpenBodyDiagram),
        QuickActionSpec("Schedule a break", "Add to your calendar", HomeAction.ScheduleBreak),
    ),
)

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
private fun CooldownCard(steps: Long, onAction: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = RoundedCornerShape(16.dp),
        onClick = onAction,
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Walked $steps steps today.", style = MaterialTheme.typography.titleMedium)
            Text(
                "Try a quick cooldown stretch to keep your back happy.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f),
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
