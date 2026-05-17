package com.lowerbackstretching.ui.settings.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The visual shell every Settings card uses: a rounded Card with a
 * 16dp inner Column. Existed inlined in every cards/*.kt file before
 * this — extracting kills ~5 lines of boilerplate per card and locks
 * the shape/padding in one place.
 *
 * `verticalSpacing` defaults to 12dp (the most common value); pass
 * 0.dp or 8.dp where a specific card needs tighter rows.
 */
@Composable
internal fun SettingsCard(
    verticalSpacing: Dp = 12.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing),
            content = content,
        )
    }
}
