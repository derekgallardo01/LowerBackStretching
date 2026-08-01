package com.lowerbackstretching.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lowerbackstretching.ui.components.ScreenHeader
import com.lowerbackstretching.ui.components.SectionHeader
import com.lowerbackstretching.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Guards the heading semantics added to the two shared header composables.
 *
 * Every screen builds its titles from `ScreenHeader` / `SectionHeader`, so these
 * two assertions cover TalkBack heading navigation app-wide. Before this, no
 * node in the app was marked as a heading and heading navigation did nothing.
 */
@RunWith(AndroidJUnit4::class)
class AccessibilityTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun isHeading() = SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading)

    @Test
    fun screen_header_is_exposed_as_a_heading() {
        rule.setContent { AppTheme { ScreenHeader("Welcome back") } }
        rule.onNodeWithText("Welcome back").assert(isHeading())
    }

    @Test
    fun section_header_is_exposed_as_a_heading() {
        rule.setContent { AppTheme { SectionHeader("Programs") } }
        rule.onNodeWithText("Programs").assert(isHeading())
    }
}
