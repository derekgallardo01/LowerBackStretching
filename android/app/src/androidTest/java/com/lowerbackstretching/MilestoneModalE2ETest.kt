package com.lowerbackstretching

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.lowerbackstretching.data.Prefs
import com.lowerbackstretching.data.db.SessionEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assume
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * Seeds 6 completed days ending yesterday, then runs a full session
 * today. After the 7th consecutive day the [MilestoneModal] should
 * surface with "7-day streak". This is the first big-milestone
 * threshold defined in `MILESTONE_THRESHOLDS`.
 */
@RunWith(AndroidJUnit4::class)
class MilestoneModalE2ETest {

    @get:Rule(order = 0)
    val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)

    @get:Rule(order = 1)
    val rule = createAndroidComposeRule<MainActivity>()

    private val ctx = InstrumentationRegistry.getInstrumentation().targetContext
    private val app = ctx.applicationContext as App

    @Before
    fun seedSixDayStreak() = runBlocking {
        // Same Assume gate as CompleteRoutineE2ETest — gesture timing inside
        // the player flakes on the tablet AVD; phone is the canonical
        // verification surface for player-driven flows.
        val sw = ctx.resources.configuration.smallestScreenWidthDp
        Assume.assumeTrue(
            "Skipping gesture-heavy E2E on tablet AVD (smallestScreenWidthDp=$sw)",
            sw < 600,
        )
        Prefs(ctx).resetForTests()
        app.database.clearAllTables()
        Prefs(ctx).markOnboardingDone()

        val dao = app.database.sessionDao()
        val today = LocalDate.now()
        // Days -6 through -1 inclusive: 6 prior consecutive days.
        for (offset in 6 downTo 1) {
            val date = today.minusDays(offset.toLong())
            dao.insert(
                SessionEntity(
                    programId = "daily-5min",
                    dayNumber = 1,
                    completedAtEpochDay = date.toEpochDay(),
                    completedAtEpochMillis = date.toEpochDay() * 86_400_000L,
                    durationSeconds = 180,
                    type = "program",
                )
            )
        }
    }

    @Test
    fun completing_session_on_seventh_day_shows_milestone_modal() {
        rule.waitUntil(timeoutMillis = 10_000) {
            rule.onAllNodesWithText("Welcome back").fetchSemanticsNodes().isNotEmpty()
        }
        rule.tapTab("Programs")
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Daily 5-Minute").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Daily 5-Minute").performClick()
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Daily Routine", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Daily Routine", substring = true).performClick()

        // Skip pre-session prompt.
        rule.waitUntil(timeoutMillis = 10_000) {
            rule.onAllNodesWithText("How's your back right now?")
                .fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Skip").performClick()

        // Hold-skip through all 4 stretches in the Daily 5-Minute day 1.
        rule.waitUntil(timeoutMillis = 10_000) {
            rule.onAllNodesWithContentDescription("Hold to skip ahead")
                .fetchSemanticsNodes().isNotEmpty()
        }
        val stretchCount = 4
        rule.waitUntil(timeoutMillis = 10_000) {
            rule.onAllNodesWithText("Stretch 1 of $stretchCount")
                .fetchSemanticsNodes().isNotEmpty()
        }
        for (i in 1..stretchCount) {
            rule.onNodeWithContentDescription("Hold to skip ahead").performTouchInput {
                longClick(durationMillis = 1500)
            }
            rule.waitForIdle()
            if (i < stretchCount) {
                val nextOf = "Stretch ${i + 1} of $stretchCount"
                rule.waitUntil(timeoutMillis = 10_000) {
                    rule.onAllNodesWithText(nextOf).fetchSemanticsNodes().isNotEmpty()
                }
            }
        }

        // Skip post-session prompt so the milestone modal can surface.
        rule.waitUntil(timeoutMillis = 15_000) {
            rule.onAllNodesWithText("How does it feel now?")
                .fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Skip").performClick()

        // The 7-day milestone modal should now overlay the FinishedView.
        // "7-day streak" appears in BOTH the StreakBadge and the modal, so we
        // anchor on the modal-specific body copy which is unique.
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("A whole week of showing up. That's how habits start.")
                .fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("A whole week of showing up. That's how habits start.")
            .assertIsDisplayed()
        rule.onNodeWithText("Continue").performClick()
    }
}

private fun AndroidComposeTestRule<*, *>.tapTab(label: String) {
    onNode(hasText(label) and hasClickAction()).performClick()
}
