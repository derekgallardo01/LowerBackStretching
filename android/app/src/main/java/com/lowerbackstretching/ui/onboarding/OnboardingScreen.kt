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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.notifications.ReminderScheduler
import com.lowerbackstretching.ui.AppViewModel
import kotlinx.coroutines.launch


private data class Page(val title: String, val body: String, val icon: ImageVector)

private val pages = listOf(
    Page(
        "Stretch with guided routines",
        "Pick a program by goal — lower back relief, hip openers, post-run cooldown. Each day plays a sequence of stretches with timers.",
        Icons.Filled.FitnessCenter,
    ),
    Page(
        "Build your own",
        "Pick any stretches from the library to build a routine that fits you. Practice single stretches anytime.",
        Icons.Filled.SelfImprovement,
    ),
    Page(
        "Stay consistent",
        "Track every session on the calendar. Streaks show your habit at a glance.",
        Icons.Filled.CalendarMonth,
    ),
    Page(
        "Daily reminder (optional)",
        "A gentle nudge once a day so you don't forget. You can change the time or turn it off later.",
        Icons.Filled.Notifications,
    ),
)

@Composable
fun OnboardingScreen(
    onDone: () -> Unit,
    vm: AppViewModel = viewModel(),
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val pager = rememberPagerState(pageCount = { pages.size })

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        HorizontalPager(
            state = pager,
            modifier = Modifier.fillMaxWidth().weight(1f),
        ) { index ->
            PageView(pages[index])
        }

        Spacer(Modifier.height(16.dp))
        DotsIndicator(currentPage = pager.currentPage, totalPages = pages.size)
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = {
                scope.launch {
                    vm.prefs.markOnboardingDone()
                    onDone()
                }
            }) {
                Text("Skip")
            }

            val isLast = pager.currentPage == pages.size - 1
            Button(onClick = {
                scope.launch {
                    if (isLast) {
                        vm.prefs.setReminder(enabled = true, hour = 8, minute = 0)
                        ReminderScheduler.schedule(ctx, 8, 0)
                        vm.prefs.markOnboardingDone()
                        onDone()
                    } else {
                        pager.animateScrollToPage(pager.currentPage + 1)
                    }
                }
            }) {
                Text(if (isLast) "Turn on reminders" else "Next")
            }
        }
    }
}

@Composable
private fun PageView(page: Page) {
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

