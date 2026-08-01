package com.lowerbackstretching.ui.programs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lowerbackstretching.R

/**
 * Bottom sheet shown when the user long-presses a custom routine row.
 * The Duplicate / Move up / Move down / Delete callbacks are wired by
 * the parent — this component is pure UI.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineActionsSheet(
    routineName: String,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onShare: () -> Unit,
    onDuplicate: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(routineName, style = MaterialTheme.typography.titleMedium)
            ActionRow(
                icon = Icons.Filled.Share,
                label = stringResource(R.string.action_share),
                onClick = onShare,
                enabled = true,
            )
            ActionRow(
                icon = Icons.Filled.ContentCopy,
                label = stringResource(R.string.action_duplicate),
                onClick = onDuplicate,
                enabled = true,
            )
            ActionRow(
                icon = Icons.Filled.ArrowUpward,
                label = stringResource(R.string.action_move_up),
                onClick = onMoveUp,
                enabled = canMoveUp,
            )
            ActionRow(
                icon = Icons.Filled.ArrowDownward,
                label = stringResource(R.string.action_move_down),
                onClick = onMoveDown,
                enabled = canMoveDown,
            )
            ActionRow(
                icon = Icons.Filled.Delete,
                label = stringResource(R.string.action_delete),
                onClick = onDelete,
                enabled = true,
            )
        }
    }
}

@Composable
private fun ActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean,
) {
    TextButton(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(icon, contentDescription = null)
            Text(label, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
