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
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Next").fetchSemanticsNodes().isNotEmpty()
        }
        repeat(3) { rule.onNodeWithText("Next").performClick() }
        rule.onNodeWithText("Turn on reminders").performClick()

        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Welcome back").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Welcome back").assertIsDisplayed()
    }
}
