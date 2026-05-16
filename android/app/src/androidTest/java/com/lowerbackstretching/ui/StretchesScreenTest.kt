package com.lowerbackstretching.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lowerbackstretching.ui.stretches.StretchesScreen
import com.lowerbackstretching.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StretchesScreenTest {

    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun shows_header_and_default_includes_all_stretches() {
        rule.setContent { AppTheme { StretchesScreen(onOpenStretch = {}) } }
        rule.onNodeWithText("Stretches").assertIsDisplayed()
        rule.onNodeWithText("Cat-Cow").assertIsDisplayed()
        rule.onNodeWithText("Pigeon Pose").assertIsDisplayed()
    }

    @Test
    fun filtering_by_calves_hides_lower_back_only_stretches() {
        rule.setContent { AppTheme { StretchesScreen(onOpenStretch = {}) } }
        rule.onNodeWithText("calves").performClick()
        // Wall Calf Stretch should show
        rule.onNodeWithText("Wall Calf Stretch").assertIsDisplayed()
        // Cat-Cow is lower-back/spine only — should disappear
        rule.onNodeWithText("Cat-Cow").assertIsNotDisplayed()
    }

    @Test
    fun all_chip_resets_filter() {
        rule.setContent { AppTheme { StretchesScreen(onOpenStretch = {}) } }
        rule.onNodeWithText("calves").performClick()
        rule.onNodeWithText("all").performClick()
        rule.onNodeWithText("Cat-Cow").assertIsDisplayed()
        rule.onNodeWithText("Wall Calf Stretch").assertIsDisplayed()
    }

    @Test
    fun clicking_stretch_invokes_callback() {
        var openedId: String? = null
        rule.setContent {
            AppTheme {
                StretchesScreen(onOpenStretch = { openedId = it })
            }
        }
        rule.onNodeWithText("Cat-Cow").performClick()
        assert(openedId == "cat-cow") { "expected cat-cow, got $openedId" }
    }
}
