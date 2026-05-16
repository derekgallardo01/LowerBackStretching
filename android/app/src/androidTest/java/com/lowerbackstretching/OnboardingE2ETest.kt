package com.lowerbackstretching

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
        // Clear DataStore in-memory state too — file delete alone isn't enough.
        Prefs(ctx).resetForTests()
    }

    @Test
    fun tapping_skip_lands_on_home_tab() {
        rule.onNodeWithText("Skip").assertIsDisplayed()
        rule.onNodeWithText("Skip").performClick()

        rule.onNodeWithText("Home").assertIsDisplayed()
        rule.onNodeWithText("Programs").assertIsDisplayed()
        rule.onNodeWithText("Calendar").assertIsDisplayed()
    }

    @Test
    fun stepping_through_all_pages_completes_onboarding() {
        repeat(3) { rule.onNodeWithText("Next").performClick() }
        rule.onNodeWithText("Turn on reminders").performClick()

        rule.onNodeWithText("Programs").assertIsDisplayed()
    }
}
