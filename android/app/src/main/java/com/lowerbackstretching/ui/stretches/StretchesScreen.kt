package com.lowerbackstretching.ui.stretches

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.data.BodyParts
import com.lowerbackstretching.data.model.Stretch
import com.lowerbackstretching.ui.AppViewModel
import com.lowerbackstretching.ui.components.ChipsRow
import com.lowerbackstretching.ui.components.InfoRow


@Composable
fun StretchesScreen(
    onOpenStretch: (String) -> Unit,
    vm: AppViewModel = viewModel(),
) {
    val all = vm.content.stretches
    val filterOptions = remember(all) { BodyParts.filterOptions(all) }
    var filter by remember { mutableStateOf(BodyParts.ALL) }
    val visible = all.filteredBy(filter)

    Column {
        Text(
            "Stretches",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
        )
        ChipsRow(options = filterOptions, selected = filter, onSelect = { filter = it })
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp, top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(visible, key = { it.id }) { stretch ->
                InfoRow(
                    title = stretch.name,
                    subtitle = stretch.shortSubtitle(),
                    onClick = { onOpenStretch(stretch.id) },
                )
            }
        }
    }
}

internal fun List<Stretch>.filteredBy(bodyPart: String): List<Stretch> =
    if (bodyPart == BodyParts.ALL) this else filter { bodyPart in it.bodyParts }

internal fun Stretch.shortSubtitle(): String =
    "${durationSeconds}s · $difficulty · ${BodyParts.displayList(bodyParts)}"
