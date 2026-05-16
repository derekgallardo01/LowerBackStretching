package com.lowerbackstretching

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lowerbackstretching.data.Prefs
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Full happy-path: clear state → mark onboarding done → navigate
 * Programs → Daily 5-Minute → Day 1 → skip through all stretches →
 * Done → Calendar → assert a "Recent sessions" entry now exists.
 */
@RunWith(AndroidJUnit4::class)
class CompleteRoutineE2ETest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    private val ctx = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun reset() = runBlocking {
        val app = ctx.applicationContext as App
        app.database.clearAllTables()
        val prefs = Prefs(ctx)
        prefs.resetForTests()
        prefs.markOnboardingDone()
    }

    @Test
    fun complete_daily_5min_routine_shows_in_calendar() {
        rule.onNodeWithText("Programs").performClick()
        rule.onNodeWithText("Daily 5-Minute").performClick()
        rule.onNodeWithText("Day 1 · Daily Routine").performClick()

        // Player loads with 4 stretches — tap Next 4 times to finish.
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithContentDescription("Next").fetchSemanticsNodes().isNotEmpty()
        }
        repeat(4) { rule.onNodeWithContentDescription("Next").performClick() }

        // Finish screen.
        rule.onNodeWithText("Nice work.").assertIsDisplayed()
        rule.onNodeWithText("Done").performClick()

        // Calendar should now show the session.
        rule.onNodeWithText("Calendar").performClick()
        rule.onNodeWithText("Recent sessions").assertIsDisplayed()
        rule.onNodeWithText("Daily 5-Minute · Day 1").assertIsDisplayed()
    }
}
