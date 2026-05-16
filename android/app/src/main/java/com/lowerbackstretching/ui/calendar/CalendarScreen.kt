package com.lowerbackstretching.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.data.CalendarMonth
import com.lowerbackstretching.data.db.SessionEntity
import com.lowerbackstretching.ui.AppViewModel
import com.lowerbackstretching.ui.components.InfoRow
import com.lowerbackstretching.ui.components.Stat
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale


@Composable
fun CalendarScreen(vm: AppViewModel = viewModel()) {
    val completed by vm.sessions.completedDays().collectAsState(initial = emptySet())
    val streak by vm.sessions.streak().collectAsState(initial = 0)
    val total by vm.sessions.count().collectAsState(initial = 0)
    val recent by vm.sessions.recent().collectAsState(initial = emptyList())
    var month by remember { mutableStateOf(YearMonth.now()) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text("Calendar", style = MaterialTheme.typography.headlineMedium) }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Stat("$streak", "Streak")
                Stat("$total", "Sessions")
                Stat("${completed.size}", "Active days")
            }
        }
        item {
            val grid = CalendarMonth(month)
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(12.dp)) {
                    MonthHeader(month, onPrev = { month = month.minusMonths(1) }, onNext = { month = month.plusMonths(1) })
                    Spacer(Modifier.height(8.dp))
                    WeekdayLabels(grid)
                    MonthGrid(grid = grid, completed = completed)
                }
            }
        }

        if (recent.isEmpty()) {
            item {
                Text(
                    "No sessions yet. Start a routine to track your progress.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        } else {
            item {
                Text(
                    "Recent sessions",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            items(recent, key = { it.id }) { session ->
                InfoRow(
                    title = session.headerTitle(vm.content.program(session.programId)?.title ?: session.programId),
                    subtitle = session.subtitle(),
                )
            }
        }
    }
}

private fun SessionEntity.headerTitle(programTitle: String): String =
    "$programTitle · Day $dayNumber"

private fun SessionEntity.subtitle(): String {
    val date = Instant.ofEpochMilli(completedAtEpochMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
    val formatted = date.format(DateTimeFormatter.ofPattern("MMM d · h:mm a", Locale.getDefault()))
    return "$formatted · ${durationSeconds / 60} min"
}

@Composable
private fun MonthHeader(month: YearMonth, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onPrev) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous")
        }
        Text(
            "${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.year}",
            style = MaterialTheme.typography.titleLarge,
        )
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next")
        }
    }
}

@Composable
private fun WeekdayLabels(grid: CalendarMonth) {
    Row(Modifier.fillMaxWidth()) {
        grid.weekdayLabels().forEach { label ->
            Text(
                label,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun MonthGrid(grid: CalendarMonth, completed: Set<LocalDate>) {
    val today = LocalDate.now()
    Column {
        grid.weeks.forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    if (grid.isInMonth(date)) {
                        DayCell(
                            date = date,
                            done = date in completed,
                            isToday = date == today,
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Spacer(Modifier.weight(1f).height(40.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(date: LocalDate, done: Boolean, isToday: Boolean, modifier: Modifier) {
    Box(
        modifier = modifier.padding(2.dp).height(44.dp),
        contentAlignment = Alignment.Center,
    ) {
        val bg = when {
            done -> MaterialTheme.colorScheme.primary
            isToday -> MaterialTheme.colorScheme.primaryContainer
            else -> androidx.compose.ui.graphics.Color.Transparent
        }
        val fg = when {
            done -> MaterialTheme.colorScheme.onPrimary
            isToday -> MaterialTheme.colorScheme.onPrimaryContainer
            else -> MaterialTheme.colorScheme.onSurface
        }
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(bg),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                date.dayOfMonth.toString(),
                color = fg,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            )
        }
    }
}
