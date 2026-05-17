package com.lowerbackstretching.ui.settings.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Row layout used by every settings card that has a Switch on the
 * trailing side: a vertically-centered Row, a Column for the label
 * (any combo of Text composables — title, optional subtitle, etc.),
 * and the Switch. Each caller controls its own typography via the
 * [label] slot; this composable owns only the structural scaffolding.
 */
@Composable
internal fun SettingsToggleRow(
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    label: @Composable ColumnScope.() -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.padding(end = 12.dp), content = label)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
