package com.lowerbackstretching.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Park
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.core.Achievement
import com.lowerbackstretching.core.pairSessionPainLogs
import com.lowerbackstretching.core.sessionPainDelta
import com.lowerbackstretching.ui.AppViewModel
import com.lowerbackstretching.ui.components.Confetti
import com.lowerbackstretching.ui.components.StreakBadge
import com.lowerbackstretching.ui.util.CelebrationCopy

/**
 * Post-session reinforcement screen. Shows a gentle confetti overlay,
 * a varied headline, and up to three reward cards (streak, level-up,
 * pain delta) depending on what actually happened in this session. The
 * Done button returns the user to the previous nav destination.
 */
@Composable
internal fun FinishedView(
    modifier: Modifier,
    onDone: () -> Unit,
    finishedSession: FinishedSessionState? = null,
    appVm: AppViewModel = viewModel(),
) {
    val headline = remember(finishedSession?.sessionId) { CelebrationCopy.pick() }
    val painLogs by appVm.painLog.all().collectAsState(initial = emptyList())
    val painPair = remember(painLogs, finishedSession?.sessionId) {
        if (finishedSession == null) null
        else pairSessionPainLogs(painLogs)
            .firstOrNull { pair ->
                // The pair's post log is the one for this session.
                pair.post.recordedAtEpochMillis > 0 &&
                    (pair.post as? com.lowerbackstretching.data.db.PainLogEntity)?.sessionId == finishedSession.sessionId
            }
    }

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = headline,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(24.dp))

            if (finishedSession != null) {
                StreakBadge(
                    streak = finishedSession.streakAfter,
                    isNewToday = finishedSession.streakIncreased,
                )
                Spacer(Modifier.height(12.dp))

                if (finishedSession.leveledUp) {
                    LevelUpCard(newLevel = finishedSession.levelAfter)
                    Spacer(Modifier.height(12.dp))
                }
            }

            if (painPair != null) {
                val delta = remember(painPair) { sessionPainDelta(painPair) }
                PainDeltaCard(pre = delta.pre, post = delta.post, change = delta.delta)
                Spacer(Modifier.height(12.dp))
            }

            finishedSession?.newlyUnlocked?.forEach { achievement ->
                UnlockCard(achievement)
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Done")
            }
            Spacer(Modifier.height(24.dp))
        }

        // Confetti overlay sits on top of everything but doesn't intercept taps.
        Confetti(modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun LevelUpCard(newLevel: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowUpward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text("Level $newLevel", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "You leveled up.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
private fun UnlockCard(achievement: Achievement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.EmojiEvents,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Unlocked: ${achievement.title}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
private fun PainDeltaCard(pre: Int?, post: Int, change: Int?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Park,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (pre != null) "Pain $pre → $post" else "Pain: $post",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                if (change != null) {
                    val label = when {
                        change < 0 -> "Better by ${-change}"
                        change > 0 -> "Up by $change"
                        else -> "Unchanged"
                    }
                    Text(
                        label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (change < 0) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }
}
