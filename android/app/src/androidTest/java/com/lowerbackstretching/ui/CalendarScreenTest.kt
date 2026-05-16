package com.lowerbackstretching.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lowerbackstretching.ui.calendar.CalendarScreen
import com.lowerbackstretching.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CalendarScreenTest {

    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun renders_header_stats_and_empty_state() {
        rule.setContent { AppTheme { CalendarScreen() } }
        rule.onNodeWithText("Calendar").assertIsDisplayed()
        rule.onNodeWithText("Streak").assertIsDisplayed()
        rule.onNodeWithText("Sessions").assertIsDisplayed()
        rule.onNodeWithText("Active days").assertIsDisplayed()
        rule.onNodeWithText("No sessions yet. Start a routine to track your progress.")
            .assertIsDisplayed()
    }
}
