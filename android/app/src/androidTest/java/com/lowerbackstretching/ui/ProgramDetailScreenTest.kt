package com.lowerbackstretching.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lowerbackstretching.ui.programs.ProgramDetailScreen
import com.lowerbackstretching.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProgramDetailScreenTest {

    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun renders_program_title_and_days() {
        rule.setContent {
            AppTheme {
                ProgramDetailScreen(
                    programId = "lower-back-relief-7day",
                    onStartDay = {},
                    onBack = {},
                )
            }
        }
        rule.onNodeWithText("Lower Back Relief").assertIsDisplayed()
        // Day 1 has a " · Today" suffix when it's the current day (default for a
        // fresh program-progress flow).
        rule.onNodeWithText("Gentle Start", substring = true).assertIsDisplayed()
        rule.onNodeWithText("Putting It Together", substring = true)
            .performScrollTo().assertIsDisplayed()
    }

    @Test
    fun tapping_day_invokes_onStartDay_with_correct_number() {
        var started: Int? = null
        rule.setContent {
            AppTheme {
                ProgramDetailScreen(
                    programId = "lower-back-relief-7day",
                    onStartDay = { started = it },
                    onBack = {},
                )
            }
        }
        rule.onNodeWithText("Hips Meet Back", substring = true)
            .performScrollTo().performClick()
        assert(started == 3) { "expected day 3, got $started" }
    }
}
