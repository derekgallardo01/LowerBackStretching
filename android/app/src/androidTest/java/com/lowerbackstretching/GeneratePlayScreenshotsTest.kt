package com.lowerbackstretching

import android.Manifest
import android.graphics.Bitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.lowerbackstretching.data.Prefs
import com.lowerbackstretching.data.db.SessionEntity
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.LocalDate

/**
 * Captures the screens we want to ship as Play Store screenshots. Each
 * @Test seeds + navigates + grabs one screen via the platform
 * UiAutomation API, then saves the PNG into the app's external cache
 * (`/sdcard/Android/data/com.lowerbackstretching/cache/play-screenshots/`).
 *
 * Outside this test, `adb pull` collects them into `screenshots/phone/`
 * or `screenshots/tablet/` depending on which AVD ran the test.
 *
 * NOT included in CI — this only runs when explicitly invoked, since
 * its job is to produce artifacts, not to assert behaviour.
 */
@RunWith(AndroidJUnit4::class)
class GeneratePlayScreenshotsTest {

    @get:Rule(order = 0)
    val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)

    @get:Rule(order = 1)
    val rule = createAndroidComposeRule<MainActivity>()

    private val ctx = InstrumentationRegistry.getInstrumentation().targetContext
    private val app = ctx.applicationContext as App

    @Before
    fun seedRichState() {
        runBlocking { seedImpl() }
    }

    private suspend fun seedImpl() {
        // We want every shot to look "lived in" — populated streak, sessions
        // visible on Home, pain history with at least one delta to display.
        Prefs(ctx).resetForTests()
        app.database.clearAllTables()
        Prefs(ctx).markOnboardingDone()
        Prefs(ctx).setLastSessionEpochDay(LocalDate.now().toEpochDay())

        val dao = app.database.sessionDao()
        val today = LocalDate.now()
        // 5 prior days of sessions for a 5-day streak.
        for (offset in 4 downTo 0) {
            val date = today.minusDays(offset.toLong())
            dao.insert(
                SessionEntity(
                    programId = "daily-5min",
                    dayNumber = 1,
                    completedAtEpochDay = date.toEpochDay(),
                    completedAtEpochMillis =
                        date.toEpochDay() * 86_400_000L + 12 * 3_600_000L,
                    durationSeconds = 180 + offset * 30,
                    type = "program",
                )
            )
        }

        // Paired pre/post pain log so PainHistoryScreen shows a "Session
        // deltas" row with an obvious improvement.
        val sessionId = 1L
        app.painLogRepository.recordPre(painLevel = 7, bodyLocationTag = "lower-back")
        Thread.sleep(50)
        app.painLogRepository.recordPost(
            painLevel = 4,
            bodyLocationTag = null,
            sessionId = sessionId,
        )
    }

    @Test fun shot_01_home() {
        rule.waitUntil(timeoutMillis = 8_000) {
            rule.onAllNodesWithText("Welcome back").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Welcome back").assertIsDisplayed()
        Thread.sleep(400)
        takeScreenshot("01-home")
    }

    @Test fun shot_02_programs() {
        rule.waitUntil(timeoutMillis = 8_000) {
            rule.onAllNodesWithText("Welcome back").fetchSemanticsNodes().isNotEmpty()
        }
        rule.tapTab("Programs")
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Daily 5-Minute").fetchSemanticsNodes().isNotEmpty()
        }
        Thread.sleep(400)
        takeScreenshot("02-programs")
    }

    @Test fun shot_03_program_detail() {
        rule.waitUntil(timeoutMillis = 8_000) {
            rule.onAllNodesWithText("Welcome back").fetchSemanticsNodes().isNotEmpty()
        }
        rule.tapTab("Programs")
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Lower Back Relief").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Lower Back Relief").performClick()
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Gentle Start", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        Thread.sleep(400)
        takeScreenshot("03-program-detail")
    }

    @Test fun shot_04_pain_history() {
        rule.waitUntil(timeoutMillis = 8_000) {
            rule.onAllNodesWithText("Welcome back").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Pain log").performClick()
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Session deltas").fetchSemanticsNodes().isNotEmpty()
        }
        Thread.sleep(400)
        takeScreenshot("04-pain-history")
    }

    @Test fun shot_05_calendar() {
        rule.waitUntil(timeoutMillis = 8_000) {
            rule.onAllNodesWithText("Welcome back").fetchSemanticsNodes().isNotEmpty()
        }
        rule.tapTab("Calendar")
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Recent sessions").fetchSemanticsNodes().isNotEmpty()
        }
        Thread.sleep(400)
        takeScreenshot("05-calendar")
    }

    @Test fun shot_06_stretches() {
        rule.waitUntil(timeoutMillis = 8_000) {
            rule.onAllNodesWithText("Welcome back").fetchSemanticsNodes().isNotEmpty()
        }
        rule.tapTab("Stretches")
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Cat-Cow").fetchSemanticsNodes().isNotEmpty()
        }
        Thread.sleep(400)
        takeScreenshot("06-stretches")
    }

    @Test fun shot_07_stretch_detail() {
        rule.waitUntil(timeoutMillis = 8_000) {
            rule.onAllNodesWithText("Welcome back").fetchSemanticsNodes().isNotEmpty()
        }
        rule.tapTab("Stretches")
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Cat-Cow").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Cat-Cow").performClick()
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("  Practice this stretch")
                .fetchSemanticsNodes().isNotEmpty()
        }
        // Give the WebView a moment to either render the video or fall
        // through to the loading skeleton.
        Thread.sleep(2_000)
        takeScreenshot("07-stretch-detail")
    }

    @Test fun shot_08_settings() {
        rule.waitUntil(timeoutMillis = 8_000) {
            rule.onAllNodesWithText("Welcome back").fetchSemanticsNodes().isNotEmpty()
        }
        rule.tapTab("Settings")
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Safety check").fetchSemanticsNodes().isNotEmpty()
        }
        Thread.sleep(400)
        takeScreenshot("08-settings")
    }

    private fun takeScreenshot(name: String) {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        val bitmap: Bitmap = automation.takeScreenshot()
        val outDir = File(ctx.externalCacheDir, "play-screenshots")
        if (!outDir.exists()) outDir.mkdirs()
        val file = File(outDir, "$name.png")
        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        bitmap.recycle()
    }
}

private fun AndroidComposeTestRule<*, *>.tapTab(label: String) {
    onNode(hasText(label) and hasClickAction()).performClick()
}
