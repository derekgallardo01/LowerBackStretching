package com.lowerbackstretching.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lowerbackstretching.ui.home.HomeScreen
import com.lowerbackstretching.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun renders_welcome_back_and_streak_card() {
        rule.setContent {
            AppTheme {
                HomeScreen(onOpenPrograms = {}, onOpenProgram = {})
            }
        }
        rule.onNodeWithText("Welcome back").assertIsDisplayed()
        rule.onNodeWithText("Day streak").assertIsDisplayed()
        rule.onNodeWithText("Sessions").assertIsDisplayed()
    }

    @Test
    fun shows_programs_section_with_built_in_programs() {
        rule.setContent {
            AppTheme {
                HomeScreen(onOpenPrograms = {}, onOpenProgram = {})
            }
        }
        rule.onNodeWithText("Programs").assertIsDisplayed()
        // First built-in program from programs.json
        rule.onNodeWithText("Lower Back Relief").assertIsDisplayed()
    }
}
