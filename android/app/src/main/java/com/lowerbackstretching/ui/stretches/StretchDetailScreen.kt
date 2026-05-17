package com.lowerbackstretching.ui.stretches

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.data.BodyParts
import com.lowerbackstretching.data.DurationUnit
import com.lowerbackstretching.data.formatDuration
import com.lowerbackstretching.ui.AppViewModel
import com.lowerbackstretching.ui.components.DifficultyDots
import com.lowerbackstretching.ui.components.YouTubePlayerView


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StretchDetailScreen(
    stretchId: String,
    onPractice: () -> Unit,
    onBack: () -> Unit,
    vm: AppViewModel = viewModel(),
) {
    val stretch = vm.content.stretch(stretchId) ?: return
    val unit by vm.prefs.durationUnit.collectAsState(initial = DurationUnit.SECONDS)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stretch.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            YouTubePlayerView(
                videoId = stretch.youtubeId,
                modifier = Modifier.fillMaxWidth(),
                autoplay = false,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    formatDuration(stretch.durationSeconds, unit),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text("·", style = MaterialTheme.typography.labelLarge)
                DifficultyDots(difficulty = stretch.difficulty)
            }
            Text(
                BodyParts.displayList(stretch.bodyParts),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(stretch.description, style = MaterialTheme.typography.bodyLarge)

            Button(
                onClick = onPractice,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Text("  Practice this stretch")
            }
        }
    }
}
