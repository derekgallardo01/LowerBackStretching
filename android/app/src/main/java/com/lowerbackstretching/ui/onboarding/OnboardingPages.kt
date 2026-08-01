package com.lowerbackstretching.ui.onboarding

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.ui.graphics.vector.ImageVector
import com.lowerbackstretching.R

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
        @StringRes val titleRes: Int,
        @StringRes val bodyRes: Int,
        val icon: ImageVector,
    ) : OnboardingPage

    data object SafetyCheck : OnboardingPage
}

internal val onboardingPages: List<OnboardingPage> = listOf(
    OnboardingPage.Standard(
        titleRes = R.string.onboarding_intro_title,
        bodyRes = R.string.onboarding_intro_body,
        icon = Icons.Filled.Favorite,
    ),
    OnboardingPage.SafetyCheck,
    OnboardingPage.Standard(
        titleRes = R.string.onboarding_build_title,
        bodyRes = R.string.onboarding_build_body,
        icon = Icons.Filled.SelfImprovement,
    ),
    OnboardingPage.Standard(
        titleRes = R.string.onboarding_consistent_title,
        bodyRes = R.string.onboarding_consistent_body,
        icon = Icons.Filled.CalendarMonth,
    ),
    OnboardingPage.Standard(
        titleRes = R.string.onboarding_reminder_title,
        bodyRes = R.string.onboarding_reminder_body,
        icon = Icons.Filled.Notifications,
    ),
)
