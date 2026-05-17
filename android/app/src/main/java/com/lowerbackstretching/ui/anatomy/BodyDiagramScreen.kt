package com.lowerbackstretching.ui.anatomy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.data.BodyZone
import com.lowerbackstretching.data.shortSubtitle
import com.lowerbackstretching.ui.AppViewModel
import com.lowerbackstretching.ui.components.InfoRow
import com.lowerbackstretching.ui.components.ScreenHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyDiagramScreen(
    onOpenStretch: (String) -> Unit,
    onBack: () -> Unit,
    vm: AppViewModel = viewModel(),
) {
    var selectedZone by remember { mutableStateOf<BodyZone?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tap where it hurts") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier.padding(inner).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ScreenHeader("Tap where you feel it")
            Text(
                "We'll suggest stretches that target that area.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                BodySilhouette(
                    modifier = Modifier.fillMaxWidth(0.55f),
                    onZoneTap = { selectedZone = it },
                )
            }
        }
    }

    selectedZone?.let { zone ->
        val matches = remember(zone, vm.content.stretches) {
            vm.content.stretches.filter { it.bodyParts.contains(zone.bodyPartTag) }
        }
        ModalBottomSheet(
            onDismissRequest = { selectedZone = null },
            sheetState = rememberModalBottomSheetState(),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(zone.displayName, style = MaterialTheme.typography.titleLarge)
                if (matches.isEmpty()) {
                    Text(
                        "No stretches in the catalog target this area yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(matches, key = { it.id }) { stretch ->
                            InfoRow(
                                title = stretch.name,
                                subtitle = stretch.shortSubtitle(),
                                onClick = {
                                    onOpenStretch(stretch.id)
                                    selectedZone = null
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
