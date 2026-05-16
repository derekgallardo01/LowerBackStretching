package com.lowerbackstretching.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lowerbackstretching.ui.programs.ProgramsScreen
import com.lowerbackstretching.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProgramsScreenTest {

    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun renders_header_and_built_in_programs() {
        rule.setContent {
            AppTheme {
                ProgramsScreen(
                    onOpenProgram = {},
                    onOpenCustomRoutine = {},
                    onCreateRoutine = {},
                )
            }
        }
        rule.onNodeWithText("Programs").assertIsDisplayed()
        rule.onNodeWithText("Lower Back Relief").assertIsDisplayed()
        rule.onNodeWithText("Hip Opener").assertIsDisplayed()
    }

    @Test
    fun new_routine_fab_invokes_callback() {
        var clicked = false
        rule.setContent {
            AppTheme {
                ProgramsScreen(
                    onOpenProgram = {},
                    onOpenCustomRoutine = {},
                    onCreateRoutine = { clicked = true },
                )
            }
        }
        rule.onNodeWithText("New routine").performClick()
        assert(clicked) { "expected onCreateRoutine to fire" }
    }

    @Test
    fun filtering_by_category_narrows_list() {
        rule.setContent {
            AppTheme {
                ProgramsScreen(
                    onOpenProgram = {},
                    onOpenCustomRoutine = {},
                    onCreateRoutine = {},
                )
            }
        }
        rule.onNodeWithText("legs").performClick()
        // Leg Flexibility is in 'legs' category
        rule.onNodeWithText("Leg Flexibility").assertIsDisplayed()
        // Lower Back Relief is 'lower-back' — should disappear
        rule.onNodeWithText("Lower Back Relief").assertIsNotDisplayed()
    }
}
