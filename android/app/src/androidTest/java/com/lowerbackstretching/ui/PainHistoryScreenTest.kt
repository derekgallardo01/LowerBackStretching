package com.lowerbackstretching.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lowerbackstretching.App
import com.lowerbackstretching.data.Prefs
import com.lowerbackstretching.ui.pain.PainHistoryScreen
import com.lowerbackstretching.ui.theme.AppTheme
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PainHistoryScreenTest {

    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    private val ctx = InstrumentationRegistry.getInstrumentation().targetContext
    private val app = ctx.applicationContext as App

    @Before
    fun reset() = runBlocking {
        Prefs(ctx).resetForTests()
        app.database.clearAllTables()
    }

    @Test
    fun empty_state_renders_when_no_logs() {
        rule.setContent { AppTheme { PainHistoryScreen(onBack = {}) } }
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("No ratings yet").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("No ratings yet").assertIsDisplayed()
        rule.onNodeWithText("Your next session will ask you how things feel.")
            .assertIsDisplayed()
    }

    @Test
    fun renders_session_delta_when_pre_and_post_logs_exist() {
        runBlocking {
            // Seed a paired pre/post for the same session — close in time, same
            // session id — so the pairing helper picks them up.
            val sessionId = 42L
            app.painLogRepository.recordPre(painLevel = 7, bodyLocationTag = "lower-back")
            Thread.sleep(20)
            app.painLogRepository.recordPost(painLevel = 4, bodyLocationTag = null, sessionId = sessionId)
        }

        rule.setContent { AppTheme { PainHistoryScreen(onBack = {}) } }

        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Session deltas").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Latest").assertIsDisplayed()
        rule.onNodeWithText("Session deltas").assertIsDisplayed()
        rule.onNodeWithText("7 → 4", substring = true).assertIsDisplayed()
    }
}
