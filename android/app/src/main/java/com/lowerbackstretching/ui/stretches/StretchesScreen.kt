package com.lowerbackstretching.ui.stretches

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.lowerbackstretching.core.BodyParts
import com.lowerbackstretching.core.DurationUnit
import com.lowerbackstretching.core.filteredBy
import com.lowerbackstretching.core.shortSubtitle
import com.lowerbackstretching.ui.AppViewModel
import com.lowerbackstretching.ui.components.ChipsRow
import com.lowerbackstretching.ui.components.InfoRow
import com.lowerbackstretching.ui.components.ScreenHeader

@Composable
fun StretchesScreen(
    onOpenStretch: (String) -> Unit,
    vm: AppViewModel = viewModel(),
) {
    val all = vm.content.stretches
    val filterOptions = remember(all) { BodyParts.filterOptions(all) }
    var filter by remember { mutableStateOf(BodyParts.ALL) }
    val visible = all.filteredBy(filter)
    val unit by vm.prefs.durationUnit.collectAsState(initial = DurationUnit.SECONDS)

    Column {
        ScreenHeader("Stretches", modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp))
        ChipsRow(options = filterOptions, selected = filter, onSelect = { filter = it })
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp, top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(visible, key = { it.id }) { stretch ->
                InfoRow(
                    title = stretch.name,
                    subtitle = stretch.shortSubtitle(unit),
                    onClick = { onOpenStretch(stretch.id) },
                )
            }
        }
    }
}
