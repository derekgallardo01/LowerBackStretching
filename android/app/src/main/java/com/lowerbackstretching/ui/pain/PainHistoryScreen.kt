package com.lowerbackstretching.ui.pain

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.core.pairSessionPainLogs
import com.lowerbackstretching.core.sessionPainDelta
import com.lowerbackstretching.ui.AppViewModel
import com.lowerbackstretching.ui.components.EmptyState
import com.lowerbackstretching.ui.components.ScreenHeader
import com.lowerbackstretching.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PainHistoryScreen(onBack: () -> Unit, vm: AppViewModel = viewModel()) {
    val history by vm.painLog.all().collectAsState(initial = emptyList())
    val pairs = remember(history) { pairSessionPainLogs(history) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pain log") },
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
            if (history.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Filled.Favorite,
                        title = "No ratings yet",
                        body = "Your next session will ask you how things feel.",
                    )
                }
                return@LazyColumn
            }

            val latest = history.first()
            item { ScreenHeader("Latest") }
            item { LatestPainCard(latest = latest) }

            if (pairs.isNotEmpty()) {
                item { SectionHeader("Session deltas") }
                items(pairs) { pair ->
                    val delta = sessionPainDelta(pair)
                    SessionDeltaRow(
                        delta = delta,
                        recordedAtEpochMillis = pair.post.recordedAtEpochMillis,
                        locationTag = pair.post.bodyLocationTag ?: pair.pre?.bodyLocationTag,
                    )
                }
            }

            if (history.size > 1) {
                item { SectionHeader("All ratings", topPadding = 8.dp) }
                items(history.drop(1)) { entry ->
                    PainHistoryRow(entry)
                }
            }
        }
    }
}
