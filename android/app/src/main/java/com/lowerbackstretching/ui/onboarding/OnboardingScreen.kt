package com.lowerbackstretching.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.data.ReminderDefaults
import com.lowerbackstretching.notifications.applyReminder
import com.lowerbackstretching.notifications.applyStreakNudge
import com.lowerbackstretching.notifications.rememberNotificationPermissionAsk
import com.lowerbackstretching.ui.AppViewModel
import com.lowerbackstretching.ui.safety.RedFlagAdvisoryScreen
import com.lowerbackstretching.ui.safety.SafetyCheckPage
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onDone: () -> Unit,
    vm: AppViewModel = viewModel(),
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val askNotificationPermission = rememberNotificationPermissionAsk()
    val pages = onboardingPages
    val pager = rememberPagerState(pageCount = { pages.size })
    val isLast = pager.currentPage == pages.size - 1
    val currentPage = pages[pager.currentPage]
    val isSafetyPage = currentPage is OnboardingPage.SafetyCheck

    var showAdvisory by remember { mutableStateOf(false) }

    fun finish(turnOnReminders: Boolean) {
        if (turnOnReminders) askNotificationPermission()
        scope.launch {
            if (turnOnReminders) {
                vm.prefs.applyReminder(
                    ctx,
                    enabled = true,
                    hour = ReminderDefaults.HOUR,
                    minute = ReminderDefaults.MINUTE,
                )
                // Streak-at-risk nudge — fires at 20:00 only if the user has an
                // active streak and hasn't stretched yet today. Shipped opt-in
                // alongside the morning reminder so users discover both at once.
                vm.prefs.applyStreakNudge(ctx, enabled = true)
            }
            vm.prefs.markOnboardingDone()
            onDone()
        }
    }

    fun advance() {
        if (isSafetyPage) {
            scope.launch { vm.prefs.setRedFlagScreeningCompletedAt(System.currentTimeMillis()) }
        }
        if (isLast) {
            finish(turnOnReminders = true)
        } else {
            scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            HorizontalPager(
                state = pager,
                modifier = Modifier.fillMaxWidth().weight(1f),
            ) { index ->
                when (val page = pages[index]) {
                    is OnboardingPage.Standard -> PageView(page)
                    is OnboardingPage.SafetyCheck -> SafetyCheckPage(
                        onOneApplies = { showAdvisory = true },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            DotsIndicator(currentPage = pager.currentPage, totalPages = pages.size)
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = { finish(turnOnReminders = false) }) { Text("Skip") }

                Button(onClick = ::advance) {
                    Text(
                        when {
                            isSafetyPage -> "None of these apply"
                            isLast -> "Turn on reminders"
                            else -> "Next"
                        }
                    )
                }
            }
        }

        if (showAdvisory) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                RedFlagAdvisoryScreen(
                    onSeenDoctor = {
                        showAdvisory = false
                        advance()
                    },
                    onContinueAnyway = {
                        showAdvisory = false
                        advance()
                    },
                )
            }
        }
    }
}

@Composable
private fun PageView(page: OnboardingPage.Standard) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(96.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        page.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(48.dp),
                    )
                }
            }
            Text(
                page.title,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                page.body,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun DotsIndicator(currentPage: Int, totalPages: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        repeat(totalPages) { i ->
            val color = if (i == currentPage) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            Box(
                Modifier
                    .padding(horizontal = 4.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}
