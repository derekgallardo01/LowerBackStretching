package com.lowerbackstretching

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
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
    @get:Rule(order = 0)
    val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)

    @get:Rule(order = 1)
    val rule = createAndroidComposeRule<MainActivity>()

    private val ctx = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun reset() =
        runBlocking {
            val app = ctx.applicationContext as App
            app.database.clearAllTables()
            val prefs = Prefs(ctx)
            prefs.resetForTests()
            prefs.markOnboardingDone()
        }

    @Test
    fun complete_daily_5min_routine_shows_rewards_then_lands_in_calendar() {
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Programs").fetchSemanticsNodes().isNotEmpty()
        }
        // "Programs" text appears in both the bottom tab and the Home
        // screen's section header. Target the clickable tab specifically.
        rule.tapTab("Programs")

        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Daily 5-Minute").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Daily 5-Minute").performClick()

        // ProgramDetail shows "Day 1 · Daily Routine" with a " · Today" suffix on
        // the current day — match by substring so either rendering works.
        rule.waitUntil(timeoutMillis = 5_000) {
            rule
                .onAllNodesWithText("Daily Routine", substring = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        rule.onNodeWithText("Daily Routine", substring = true).performClick()

        // First session of the day → pre-session pain prompt shows. Skip past it
        // so the engine starts.
        rule.waitUntil(timeoutMillis = 10_000) {
            rule.onAllNodesWithText("How's your back right now?").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Skip").performClick()

        // Player loads with 4 stretches. The skip-ahead control is a simple
        // tap now ("Next stretch") — no hold gesture.
        rule.waitUntil(timeoutMillis = 10_000) {
            rule
                .onAllNodesWithContentDescription("Next stretch")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        val stretchCount = 4
        rule.waitUntil(timeoutMillis = 10_000) {
            rule
                .onAllNodesWithText("Stretch 1 of $stretchCount")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        for (i in 1..stretchCount) {
            rule.onNodeWithContentDescription("Next stretch").performClick()
            rule.waitForIdle()
            if (i < stretchCount) {
                val nextOf = "Stretch ${i + 1} of $stretchCount"
                rule.waitUntil(timeoutMillis = 10_000) {
                    rule.onAllNodesWithText(nextOf).fetchSemanticsNodes().isNotEmpty()
                }
            }
        }

        // Post-session pain prompt overlays the FinishedView. Skip it so the
        // FinishedView is interactable.
        rule.waitUntil(timeoutMillis = 15_000) {
            rule.onAllNodesWithText("How does it feel now?").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Skip").performClick()

        // FinishedView reward callouts: streak count + first-time achievement.
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("1-day streak").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("1-day streak").assertIsDisplayed()
        rule.onNodeWithText("Unlocked: First steps").assertIsDisplayed()
        rule.onNodeWithText("Done").performClick()

        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Calendar").fetchSemanticsNodes().isNotEmpty()
        }
        rule.tapTab("Calendar")
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Recent sessions").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Recent sessions").assertIsDisplayed()
        rule.onNodeWithText("Daily 5-Minute · Day 1").assertIsDisplayed()
    }
}

/**
 * Click a bottom-bar tab by label. Disambiguates from inline screen text
 * with the same label (e.g. the Home screen's "Programs" section header)
 * by requiring the node to have an onClick action — only the tab does.
 */
private fun AndroidComposeTestRule<*, *>.tapTab(label: String) {
    onNode(hasText(label) and hasClickAction()).performClick()
}
