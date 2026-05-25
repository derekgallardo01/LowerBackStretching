package com.lowerbackstretching.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lowerbackstretching.data.Prefs
import com.lowerbackstretching.ui.settings.SettingsScreen
import com.lowerbackstretching.ui.theme.AppTheme
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun reset() = runBlocking {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        Prefs(ctx).resetForTests()
    }

    @Test
    fun renders_reminder_section_and_about() {
        rule.setContent { AppTheme { SettingsScreen() } }
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Settings").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Settings").assertIsDisplayed()
        // Above-the-fold cards on a typical phone: SafetyCard, Reminder.
        rule.onNodeWithText("Safety check").assertIsDisplayed()
        rule.onNodeWithText("Daily reminder").assertIsDisplayed()
        rule.onNodeWithText("Reminder time").assertIsDisplayed()
        // Defaults from resetForTests: hour=8, minute=0.
        rule.onNodeWithText("08:00").assertIsDisplayed()
        // Below-the-fold cards: assert composition exists (the scrollable
        // Column has these but they may not be in view on small screens).
        rule.onNodeWithText("About").assertExists()
    }
}
