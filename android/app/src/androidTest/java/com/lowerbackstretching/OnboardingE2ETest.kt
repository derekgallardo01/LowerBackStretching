package com.lowerbackstretching

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
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

@RunWith(AndroidJUnit4::class)
class OnboardingE2ETest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @Before
    fun reset() = runBlocking {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        Prefs(ctx).resetForTests()
    }

    @Test
    fun tapping_skip_lands_on_home_tab() {
        // AppNav reads onboardingDone as a Flow with `initial = null`, so it
        // renders nothing for a frame or two before the OnboardingScreen
        // appears. Wait for the Skip button to show up before tapping it.
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Skip").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Skip").performClick()

        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Home").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Home").assertIsDisplayed()
        rule.onNodeWithText("Programs").assertIsDisplayed()
        rule.onNodeWithText("Calendar").assertIsDisplayed()
    }

    @Test
    fun stepping_through_all_pages_completes_onboarding() {
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Next").fetchSemanticsNodes().isNotEmpty()
        }
        repeat(3) { rule.onNodeWithText("Next").performClick() }
        rule.onNodeWithText("Turn on reminders").performClick()

        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Programs").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Programs").assertIsDisplayed()
    }
}
