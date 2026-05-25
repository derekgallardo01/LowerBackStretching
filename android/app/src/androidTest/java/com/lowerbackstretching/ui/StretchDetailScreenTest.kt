package com.lowerbackstretching.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lowerbackstretching.ui.stretches.StretchDetailScreen
import com.lowerbackstretching.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StretchDetailScreenTest {

    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun renders_stretch_details_and_practice_button() {
        rule.setContent {
            AppTheme {
                StretchDetailScreen(stretchId = "cat-cow", onPractice = {}, onBack = {})
            }
        }
        rule.onNodeWithText("Cat-Cow").assertIsDisplayed()
        rule.onNodeWithText("  Practice this stretch").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun practice_button_invokes_callback() {
        var practiced = false
        rule.setContent {
            AppTheme {
                StretchDetailScreen(
                    stretchId = "cat-cow",
                    onPractice = { practiced = true },
                    onBack = {},
                )
            }
        }
        rule.onNodeWithText("  Practice this stretch").performScrollTo().performClick()
        assert(practiced) { "expected onPractice to fire" }
    }
}
