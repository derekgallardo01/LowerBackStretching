package com.lowerbackstretching.ui

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.lowerbackstretching.MainActivity
import com.lowerbackstretching.data.Prefs
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies the user can re-review the red-flag advisory any time from
 * the Settings tab — distinct from the onboarding flow where it shows
 * inline as an overlay. The Settings entry navigates via the AppNav
 * route `Dest.safetyAdvisory`.
 */
@RunWith(AndroidJUnit4::class)
class SafetyAdvisoryE2ETest {

    @get:Rule(order = 0)
    val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)

    @get:Rule(order = 1)
    val rule = createAndroidComposeRule<MainActivity>()

    @Before
    fun reset() = runBlocking {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        Prefs(ctx).resetForTests()
        Prefs(ctx).markOnboardingDone()
    }

    @Test
    fun settings_safety_card_navigates_to_advisory_and_back() {
        rule.waitUntil(timeoutMillis = 10_000) {
            rule.onAllNodesWithText("Welcome back").fetchSemanticsNodes().isNotEmpty()
        }
        rule.tapTab("Settings")

        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Safety check").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Safety check").performClick()

        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Please see a doctor before stretching.")
                .fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Please see a doctor before stretching.").assertIsDisplayed()
        rule.onNodeWithText("I've already seen a doctor").assertIsDisplayed()
        rule.onNodeWithText("Continue anyway").assertIsDisplayed()

        // Dismissing returns to Settings — the Safety card is visible again.
        rule.onNodeWithText("Continue anyway").performClick()
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Daily reminder").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Daily reminder").assertIsDisplayed()
    }
}

/** Tap a bottom-bar tab by label, disambiguating from inline screen text. */
private fun AndroidComposeTestRule<*, *>.tapTab(label: String) {
    onNode(hasText(label) and hasClickAction()).performClick()
}
