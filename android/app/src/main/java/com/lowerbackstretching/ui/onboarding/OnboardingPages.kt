package com.lowerbackstretching.ui.onboarding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Static content for the first-launch flow. Pulled out of `OnboardingScreen`
 * so the screen file is only responsible for layout. Two page kinds:
 *
 *  - [Standard] is the icon + title + body card the original flow used.
 *  - [SafetyCheck] is a custom interactive page that lists red-flag
 *    symptoms and lets the user route to an advisory; rendered by
 *    [com.lowerbackstretching.ui.safety.SafetyCheckPage].
 */
internal sealed interface OnboardingPage {
    data class Standard(
        val title: String,
        val body: String,
        val icon: ImageVector,
    ) : OnboardingPage

    data object SafetyCheck : OnboardingPage
}

internal val onboardingPages: List<OnboardingPage> = listOf(
    OnboardingPage.Standard(
        title = "Less back pain, day by day.",
        body = "Short guided routines you can fit between meetings — built around your lower back, hips, and posture.",
        icon = Icons.Filled.Favorite,
    ),
    OnboardingPage.SafetyCheck,
    OnboardingPage.Standard(
        title = "Build your own",
        body = "Pick any stretches from the library to build a routine that fits you. Practice single stretches anytime.",
        icon = Icons.Filled.SelfImprovement,
    ),
    OnboardingPage.Standard(
        title = "Stay consistent",
        body = "Track every session on the calendar. Streaks show your habit at a glance.",
        icon = Icons.Filled.CalendarMonth,
    ),
    OnboardingPage.Standard(
        title = "Daily reminder (optional)",
        body = "A gentle nudge once a day so you don't forget. You can change the time or turn it off later.",
        icon = Icons.Filled.Notifications,
    ),
)
