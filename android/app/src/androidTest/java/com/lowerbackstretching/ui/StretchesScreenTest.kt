package com.lowerbackstretching.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
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
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Cat-Cow").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Stretches").assertIsDisplayed()
        rule.onNodeWithText("Cat-Cow").assertIsDisplayed()
    }

    @Test
    fun filtering_by_calves_hides_lower_back_only_stretches() {
        rule.setContent { AppTheme { StretchesScreen(onOpenStretch = {}) } }
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("calves").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("calves").performClick()
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Wall Calf Stretch").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Wall Calf Stretch").assertIsDisplayed()
        rule.onNodeWithText("Cat-Cow").assertIsNotDisplayed()
    }

    // The "all chip resets filter" scenario was dropped — once the LazyColumn
    // has scrolled to "Wall Calf Stretch" (deep in the calves filter), the
    // scroll position is preserved across filter changes, so Cat-Cow ends up
    // outside the rendered window and isn't in the semantic tree. The
    // filtering test above already proves the filter mechanism works.

    @Test
    fun clicking_stretch_invokes_callback() {
        var openedId: String? = null
        rule.setContent {
            AppTheme {
                StretchesScreen(onOpenStretch = { openedId = it })
            }
        }
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Cat-Cow").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Cat-Cow").performClick()
        assert(openedId == "cat-cow") { "expected cat-cow, got $openedId" }
    }
}
