package com.lowerbackstretching

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
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

@RunWith(AndroidJUnit4::class)
class OnboardingE2ETest {

    @get:Rule(order = 0)
    val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)

    @get:Rule(order = 1)
    val rule = createAndroidComposeRule<MainActivity>()

    @Before
    fun reset() = runBlocking {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        Prefs(ctx).resetForTests()
    }

    @Test
    fun tapping_skip_lands_on_home_tab() {
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Skip").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Skip").performClick()

        // "Welcome back" is unique to the Home screen content, so it's a
        // clean post-onboarding signal. (The bottom-tab "Home" is also
        // unique, but the screen header is the more visible smoke test.)
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Welcome back").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Welcome back").assertIsDisplayed()
    }

    @Test
    fun stepping_through_all_pages_completes_onboarding() {
        // Page order: value-hook → safety-check → build-your-own → consistency → reminder.
        // Page 2 (safety-check) uses "None of these apply" instead of "Next".
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Next").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Next").performClick()
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("None of these apply").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("None of these apply").performClick()
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Next").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Next").performClick()
        rule.onNodeWithText("Next").performClick()
        rule.onNodeWithText("Turn on reminders").performClick()

        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Welcome back").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Welcome back").assertIsDisplayed()
    }

    @Test
    fun tapping_one_or_more_applies_on_safety_check_opens_advisory_then_dismisses_back() {
        // Advance to the safety check page.
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Next").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Next").performClick()
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("One or more applies").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("One or more applies").performClick()

        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Please see a doctor before stretching.")
                .fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("I've already seen a doctor").performClick()

        // Advisory dismissed → onboarding advances to the next page.
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Build your own").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Build your own").assertIsDisplayed()
    }
}
