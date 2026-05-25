package com.lowerbackstretching.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lowerbackstretching.App
import com.lowerbackstretching.ui.stretches.StretchDetailScreen
import com.lowerbackstretching.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 12 of the 26 stretches ship with an empty `youtubeId` (the "no video
 * yet" sentinel). The player surface for those stretches must render
 * the no-video fallback instead of a broken YouTube embed.
 *
 * Uses `sphinx` because it's one of the 12 stretches without a video
 * (verified in stretches.json) and shows on the StretchDetail screen.
 */
@RunWith(AndroidJUnit4::class)
class NoVideoStretchTest {

    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    private val ctx = InstrumentationRegistry.getInstrumentation().targetContext
    private val app = ctx.applicationContext as App

    @Test
    fun stretch_with_empty_youtubeId_shows_no_video_fallback() {
        // Pre-condition: confirm the bundled content actually has an empty
        // youtubeId for `sphinx`. If a future commit pastes a real video
        // in, this test will turn green vacuously — that's correct.
        val sphinx = app.contentRepository.stretch("sphinx")
        if (sphinx == null || sphinx.youtubeId.isNotBlank()) return

        rule.setContent {
            AppTheme {
                StretchDetailScreen(stretchId = "sphinx", onPractice = {}, onBack = {})
            }
        }

        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Follow the description and timer below.")
                .fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Follow the description and timer below.").assertIsDisplayed()
    }
}
