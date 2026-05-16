package com.lowerbackstretching.ui.onboarding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Static content for the first-launch flow. Pulled out of `OnboardingScreen`
 * so the screen file is only responsible for layout.
 */
internal data class OnboardingPage(
    val title: String,
    val body: String,
    val icon: ImageVector,
)

internal val onboardingPages = listOf(
    OnboardingPage(
        title = "Stretch with guided routines",
        body = "Pick a program by goal — lower back relief, hip openers, post-run cooldown. Each day plays a sequence of stretches with timers.",
        icon = Icons.Filled.FitnessCenter,
    ),
    OnboardingPage(
        title = "Build your own",
        body = "Pick any stretches from the library to build a routine that fits you. Practice single stretches anytime.",
        icon = Icons.Filled.SelfImprovement,
    ),
    OnboardingPage(
        title = "Stay consistent",
        body = "Track every session on the calendar. Streaks show your habit at a glance.",
        icon = Icons.Filled.CalendarMonth,
    ),
    OnboardingPage(
        title = "Daily reminder (optional)",
        body = "A gentle nudge once a day so you don't forget. You can change the time or turn it off later.",
        icon = Icons.Filled.Notifications,
    ),
)
