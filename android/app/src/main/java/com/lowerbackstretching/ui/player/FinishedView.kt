package com.lowerbackstretching.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** End-of-routine "nice work, session logged" screen with a Done button. */
@Composable
internal fun FinishedView(modifier: Modifier, onDone: () -> Unit) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Nice work.", style = MaterialTheme.typography.headlineLarge)
            Text("Session logged.", style = MaterialTheme.typography.bodyLarge)
            Button(onClick = onDone) { Text("Done") }
        }
    }
}
