package com.lowerbackstretching.ui.settings.cards

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lowerbackstretching.BuildConfig
import com.lowerbackstretching.R

@Composable
fun AboutCard() {
    SettingsCard(verticalSpacing = 0.dp) {
        Text(stringResource(R.string.settings_about), style = MaterialTheme.typography.titleMedium)
        Text(
            stringResource(R.string.settings_about_version, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
    }
}
