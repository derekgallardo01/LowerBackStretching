package com.lowerbackstretching.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lowerbackstretching.ui.routines.RoutineBuilderScreen
import com.lowerbackstretching.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoutineBuilderE2ETest {

    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun renders_with_disabled_save_until_name_and_stretches_present() {
        var saved = false
        rule.setContent {
            AppTheme {
                RoutineBuilderScreen(onSaved = { saved = true }, onBack = {})
            }
        }
        rule.onNodeWithText("New routine").assertIsDisplayed()
        rule.onNodeWithText("Routine name").assertIsDisplayed()

        // Tap save while disabled — shouldn't navigate away.
        rule.onNodeWithContentDescription("Save").performClick()
        assert(!saved) { "save should be disabled before name and selection" }

        // Type a name.
        rule.onNodeWithText("Routine name").performTextInput("Morning")

        // Pick a stretch.
        rule.onNodeWithText("Cat-Cow").performClick()

        // Now save.
        rule.onNodeWithContentDescription("Save").performClick()
        rule.waitUntil(timeoutMillis = 3_000) { saved }
    }

    @Test
    fun filtering_picker_narrows_visible_stretches() {
        rule.setContent {
            AppTheme {
                RoutineBuilderScreen(onSaved = {}, onBack = {})
            }
        }
        rule.onNodeWithText("calves").performClick()
        rule.onNodeWithText("Wall Calf Stretch").assertIsDisplayed()
    }
}
