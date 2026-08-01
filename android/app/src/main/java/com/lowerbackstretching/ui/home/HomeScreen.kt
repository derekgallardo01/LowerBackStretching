package com.lowerbackstretching.ui.home

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.R
import com.lowerbackstretching.core.InProgressSession
import com.lowerbackstretching.core.SessionType
import com.lowerbackstretching.core.SyntheticProgramId
import com.lowerbackstretching.core.subtitle
import com.lowerbackstretching.core.xpForSession
import com.lowerbackstretching.core.xpProgress
import com.lowerbackstretching.data.ContentRepository
import com.lowerbackstretching.data.db.CustomRoutineEntity
import com.lowerbackstretching.ui.AppViewModel
import com.lowerbackstretching.ui.components.AnimatedStat
import com.lowerbackstretching.ui.components.InfoRow
import com.lowerbackstretching.ui.components.ScreenHeader
import com.lowerbackstretching.ui.components.SectionHeader
import com.lowerbackstretching.ui.components.Stat
import com.lowerbackstretching.ui.components.pressScale

/**
 * Every navigation / system-intent the Home screen can trigger.
 * AppNav owns the routing; HomeScreen just emits the intent.
 */
sealed interface HomeAction {
    data class OpenProgram(
        val id: String,
    ) : HomeAction

    data object OpenAchievements : HomeAction

    data object OpenGoals : HomeAction

    data object OpenFlexibility : HomeAction

    data object OpenPainHistory : HomeAction

    data object OpenGlossary : HomeAction

    data object OpenBodyDiagram : HomeAction

    data object ScheduleBreak : HomeAction

    /** Re-enter the player for a routine the user started but didn't finish. */
    data class ResumeSession(
        val session: InProgressSession,
    ) : HomeAction
}

@Composable
fun HomeScreen(
    onAction: (HomeAction) -> Unit,
    vm: AppViewModel = viewModel(),
) {
    val streak by vm.streak.collectAsStateWithLifecycle()
    val total by vm.sessionCount.collectAsStateWithLifecycle()
    val totalSeconds by vm.totalDurationSeconds.collectAsStateWithLifecycle()
    val xp = remember(totalSeconds) { xpProgress(xpForSession(totalSeconds)) }

    val inProgress by vm.prefs.inProgressSession.collectAsState(initial = null)
    val routines by vm.routines.collectAsStateWithLifecycle()
    val resumable = remember(inProgress, routines) {
        inProgress?.let { resumableSession(it, vm.content, routines) }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { ScreenHeader(stringResource(R.string.home_welcome)) }
        resumable?.let { resume ->
            item {
                ResumeCard(
                    title = resume.title,
                    onClick = { onAction(HomeAction.ResumeSession(resume.session)) },
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
                        title = stringResource(quick.titleRes),
                        body = stringResource(quick.subtitleRes),
                        onClick = { onAction(quick.action) },
                    )
                }
            }
        }
        item { SectionHeader(stringResource(R.string.home_section_programs)) }
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

/** An [InProgressSession] we were able to resolve to a still-existing routine. */
internal data class ResumableSession(
    val session: InProgressSession,
    val title: String,
)

/**
 * Resolve a persisted [InProgressSession] to something we can actually show and
 * navigate to. Returns null when the underlying program / stretch / custom
 * routine no longer exists — a routine can be deleted, or content can change
 * between app versions, and a "Resume" card that dead-ends is worse than none.
 */
internal fun resumableSession(
    session: InProgressSession,
    content: ContentRepository,
    routines: List<CustomRoutineEntity>,
): ResumableSession? {
    val title = when (SyntheticProgramId.typeFor(session.programId)) {
        SessionType.PROGRAM -> content.program(session.programId)?.title
        SessionType.SINGLE ->
            SyntheticProgramId
                .stretchIdFrom(session.programId)
                ?.let { content.stretch(it)?.name }
        SessionType.ROUTINE ->
            SyntheticProgramId
                .routineIdFrom(session.programId)
                ?.let { rid -> routines.firstOrNull { it.id == rid }?.name }
    } ?: return null
    return ResumableSession(session, title)
}

@Composable
private fun ResumeCard(
    title: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().pressScale(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        ),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null)
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.home_resume_title), style = MaterialTheme.typography.titleMedium)
                Text(
                    title,
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalContentColor.current.copy(alpha = 0.8f),
                )
            }
        }
    }
}

private data class QuickActionSpec(
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    val action: HomeAction,
)

private val quickActionRows: List<List<QuickActionSpec>> = listOf(
    listOf(
        QuickActionSpec(R.string.home_quick_goals, R.string.home_quick_goals_sub, HomeAction.OpenGoals),
        QuickActionSpec(
            R.string.home_quick_achievements,
            R.string.home_quick_achievements_sub,
            HomeAction.OpenAchievements,
        ),
    ),
    listOf(
        QuickActionSpec(R.string.home_quick_pain, R.string.home_quick_pain_sub, HomeAction.OpenPainHistory),
        QuickActionSpec(
            R.string.home_quick_flexibility,
            R.string.home_quick_flexibility_sub,
            HomeAction.OpenFlexibility,
        ),
    ),
    listOf(
        QuickActionSpec(R.string.home_quick_glossary, R.string.home_quick_glossary_sub, HomeAction.OpenGlossary),
        QuickActionSpec(
            R.string.home_quick_body_diagram,
            R.string.home_quick_body_diagram_sub,
            HomeAction.OpenBodyDiagram,
        ),
    ),
    listOf(
        QuickActionSpec(
            R.string.home_quick_schedule,
            R.string.home_quick_schedule_sub,
            HomeAction.ScheduleBreak,
        ),
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
                AnimatedStat(value = streak, label = stringResource(R.string.home_stat_streak))
                AnimatedStat(value = total, label = stringResource(R.string.home_stat_sessions))
                Stat(value = "L$level", label = stringResource(R.string.home_stat_level))
            }
            val animatedXp by animateFloatAsState(
                targetValue = xpProgress.coerceIn(0f, 1f),
                animationSpec = tween(durationMillis = 600),
                label = "xpProgress",
            )
            LinearProgressIndicator(
                progress = { animatedXp },
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
    Card(modifier = modifier.fillMaxWidth().pressScale(), shape = RoundedCornerShape(16.dp), onClick = onClick) {
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
