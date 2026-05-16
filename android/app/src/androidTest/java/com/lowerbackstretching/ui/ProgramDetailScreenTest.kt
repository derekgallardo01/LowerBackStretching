package com.lowerbackstretching.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
        rule.onNodeWithText("Day 1 · Gentle Start").assertIsDisplayed()
        rule.onNodeWithText("Day 7 · Putting It Together").assertIsDisplayed()
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
        rule.onNodeWithText("Day 3 · Hips Meet Back").performClick()
        assert(started == 3) { "expected day 3, got $started" }
    }
}
