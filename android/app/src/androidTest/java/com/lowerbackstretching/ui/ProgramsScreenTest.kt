package com.lowerbackstretching.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lowerbackstretching.App
import com.lowerbackstretching.data.Prefs
import com.lowerbackstretching.ui.programs.ProgramsScreen
import com.lowerbackstretching.ui.theme.AppTheme
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProgramsScreenTest {

    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun reset() = runBlocking {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        Prefs(ctx).resetForTests()
        (ctx.applicationContext as App).database.clearAllTables()
    }

    @Test
    fun renders_header_and_first_built_in_program() {
        rule.setContent {
            AppTheme {
                ProgramsScreen(
                    onOpenProgram = {},
                    onOpenCustomRoutine = {},
                    onCreateRoutine = {},
                )
            }
        }
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Lower Back Relief").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Programs").assertIsDisplayed()
        rule.onNodeWithText("Lower Back Relief").assertIsDisplayed()
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
        // The FAB's text node lives in the unmerged semantic tree (Material 3
        // FloatingActionButton hides it from the merged tree). Search there.
        rule.onNode(
            hasText("New routine") and hasClickAction(),
            useUnmergedTree = true,
        ).performClick()
        assert(clicked) { "expected onCreateRoutine to fire" }
    }

    @Test
    fun filtering_by_category_changes_visible_programs() {
        rule.setContent {
            AppTheme {
                ProgramsScreen(
                    onOpenProgram = {},
                    onOpenCustomRoutine = {},
                    onCreateRoutine = {},
                )
            }
        }
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("legs").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("legs").performClick()
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Leg Flexibility").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Leg Flexibility").assertIsDisplayed()
    }
}
