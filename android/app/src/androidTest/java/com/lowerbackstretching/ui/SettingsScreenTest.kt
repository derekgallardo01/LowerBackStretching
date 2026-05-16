package com.lowerbackstretching.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lowerbackstretching.ui.settings.SettingsScreen
import com.lowerbackstretching.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun renders_reminder_section_and_about() {
        rule.setContent { AppTheme { SettingsScreen() } }
        rule.onNodeWithText("Settings").assertIsDisplayed()
        rule.onNodeWithText("Daily reminder").assertIsDisplayed()
        rule.onNodeWithText("Reminder time").assertIsDisplayed()
        rule.onNodeWithText("About").assertIsDisplayed()
        // Initial reminder time is 08:00 from defaults
        rule.onNodeWithText("08:00").assertIsDisplayed()
    }
}
