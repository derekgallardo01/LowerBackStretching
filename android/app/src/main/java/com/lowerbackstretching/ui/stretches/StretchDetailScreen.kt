package com.lowerbackstretching.ui.stretches

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.ui.AppViewModel
import com.lowerbackstretching.ui.components.YouTubePlayerView

class StretchDetailViewModel(app: Application) : AppViewModel(app)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StretchDetailScreen(
    stretchId: String,
    onBack: () -> Unit,
    vm: StretchDetailViewModel = viewModel(),
) {
    val stretch = vm.content.stretch(stretchId) ?: return

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
            Text(
                "${stretch.durationSeconds}s · ${stretch.difficulty}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                stretch.bodyParts.joinToString(" · ") { it.replace('-', ' ') },
                style = MaterialTheme.typography.labelLarge,
            )
            Text(stretch.description, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
