package com.lowerbackstretching.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lowerbackstretching.ui.components.ScreenHeader
import com.lowerbackstretching.ui.settings.cards.AboutCard
import com.lowerbackstretching.ui.settings.cards.AppearanceCard
import com.lowerbackstretching.ui.settings.cards.AudioCard
import com.lowerbackstretching.ui.settings.cards.CloudSyncCard
import com.lowerbackstretching.ui.settings.cards.HapticsCard
import com.lowerbackstretching.ui.settings.cards.HealthCard
import com.lowerbackstretching.ui.settings.cards.ReminderCard

@Composable
fun SettingsScreen() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ScreenHeader("Settings")
        ReminderCard()
        AppearanceCard()
        HapticsCard()
        AudioCard()
        HealthCard()
        CloudSyncCard()
        AboutCard()
    }
}
