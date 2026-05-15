package com.lowerbackstretching

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.lowerbackstretching.data.Prefs

@RunWith(AndroidJUnit4::class)
class OnboardingE2ETest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @Before
    fun reset() {
        // Clear DataStore so onboarding always shows for the test.
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        ctx.dataStoreFile("settings").delete()
    }

    @After
    fun cleanup() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        runBlocking { Prefs(ctx).markOnboardingDone() }
    }

    @Test
    fun tapping_skip_lands_on_home_tab() {
        rule.onNodeWithText("Skip").assertIsDisplayed()
        rule.onNodeWithText("Skip").performClick()

        // After dismiss, we should see the bottom-tab labels.
        rule.onNodeWithText("Home").assertIsDisplayed()
        rule.onNodeWithText("Programs").assertIsDisplayed()
        rule.onNodeWithText("Calendar").assertIsDisplayed()
    }

    @Test
    fun stepping_through_all_pages_completes_onboarding() {
        // 4 pages: tap Next 3 times, then "Turn on reminders".
        repeat(3) {
            rule.onNodeWithText("Next").performClick()
        }
        rule.onNodeWithText("Turn on reminders").performClick()

        // Main UI should now be visible.
        rule.onNodeWithText("Programs").assertIsDisplayed()
    }
}

private fun android.content.Context.dataStoreFile(name: String) =
    java.io.File(filesDir, "datastore/$name.preferences_pb")
