package com.lowerbackstretching.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lowerbackstretching.App
import com.lowerbackstretching.data.Prefs
import com.lowerbackstretching.ui.calendar.CalendarScreen
import com.lowerbackstretching.ui.theme.AppTheme
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CalendarScreenTest {

    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun reset() = runBlocking {
        // Reset Room and DataStore so the empty-state branch is what renders.
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        Prefs(ctx).resetForTests()
        (ctx.applicationContext as App).database.clearAllTables()
    }

    @Test
    fun renders_header_stats_and_empty_state() {
        rule.setContent { AppTheme { CalendarScreen() } }
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Calendar").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Calendar").assertIsDisplayed()
        rule.onNodeWithText("Streak").assertIsDisplayed()
        rule.onNodeWithText("Sessions").assertIsDisplayed()
        rule.onNodeWithText("Active days").assertIsDisplayed()
        rule.onNodeWithText("Your consistency journey starts here.").assertIsDisplayed()
    }
}
