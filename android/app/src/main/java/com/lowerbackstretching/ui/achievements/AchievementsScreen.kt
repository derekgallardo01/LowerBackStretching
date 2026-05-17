package com.lowerbackstretching.ui.achievements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.data.AchievementStatus
import com.lowerbackstretching.data.evaluateAchievements
import com.lowerbackstretching.data.levelFor
import com.lowerbackstretching.data.xpForSession
import com.lowerbackstretching.ui.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(onBack: () -> Unit, vm: AppViewModel = viewModel()) {
    val totalSessions by vm.sessions.count().collectAsState(initial = 0)
    val longestStreak by vm.sessions.longestStreak().collectAsState(initial = 0)
    val totalSeconds by vm.sessions.totalDurationSeconds().collectAsState(initial = 0)
    val level = remember(totalSeconds) { levelFor(xpForSession(totalSeconds)) }

    val statuses = remember(totalSessions, longestStreak, level) {
        evaluateAchievements(totalSessions, longestStreak, level)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Achievements") },
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
            items(statuses, key = { it.achievement.id }) { AchievementRow(it) }
        }
    }
}

@Composable
private fun AchievementRow(status: AchievementStatus) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Badge(unlocked = status.unlocked)
            Column(modifier = Modifier.weight(1f)) {
                Text(status.achievement.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    status.achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
                if (!status.unlocked) {
                    LinearProgressIndicator(
                        progress = { status.progress.toFloat() / status.achievement.target },
                        modifier = Modifier.padding(top = 6.dp).fillMaxWidth(),
                    )
                    Text(
                        "${status.progress} / ${status.achievement.target}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun Badge(unlocked: Boolean) {
    Surface(
        modifier = Modifier.size(56.dp),
        shape = CircleShape,
        color = if (unlocked) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                if (unlocked) Icons.Filled.EmojiEvents else Icons.Filled.Lock,
                contentDescription = if (unlocked) "Unlocked" else "Locked",
                tint = if (unlocked) MaterialTheme.colorScheme.onPrimaryContainer
                       else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
