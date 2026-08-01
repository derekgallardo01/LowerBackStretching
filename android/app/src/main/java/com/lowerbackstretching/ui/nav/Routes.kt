package com.lowerbackstretching.ui.nav

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.lowerbackstretching.R

/**
 * Single source of truth for navigation routes. Each [Tab] is a bottom-bar
 * tab; each [Dest] is a destination (some templated, some literal). Detail
 * destinations expose a `path(args)` builder so the call site never hand-
 * builds a route string.
 */
sealed class Tab(
    val path: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    data object Home : Tab("home", R.string.tab_home, Icons.Filled.Home)

    data object Programs : Tab("programs", R.string.tab_programs, Icons.Filled.FitnessCenter)

    data object Stretches : Tab("stretches", R.string.tab_stretches, Icons.Filled.SelfImprovement)

    data object Calendar : Tab("calendar", R.string.tab_calendar, Icons.Filled.CalendarMonth)

    data object Settings : Tab("settings", R.string.tab_settings, Icons.Filled.Settings)

    companion object {
        val all = listOf(Home, Programs, Stretches, Calendar, Settings)
    }
}

// Route templates are camelCase to read as identifiers alongside their
// `fun program(id)` builders; ktlint wants SCREAMING_SNAKE for const vals.
@Suppress("ktlint:standard:property-naming")
object Dest {
    const val programTemplate = "program/{id}"

    fun program(id: String) = "program/$id"

    const val playerTemplate = "player/{id}/{day}"

    fun player(
        programId: String,
        day: Int,
    ) = "player/$programId/$day"

    const val stretchTemplate = "stretch/{id}"

    fun stretch(id: String) = "stretch/$id"

    const val singlePlayerTemplate = "player/single/{id}"

    fun singlePlayer(stretchId: String) = "player/single/$stretchId"

    const val routineNew = "routine/new"

    const val routinePlayerTemplate = "routine/{id}/play"

    fun routinePlayer(routineId: Long) = "routine/$routineId/play"

    const val achievements = "achievements"
    const val goals = "goals"
    const val flexibility = "flexibility"
    const val painHistory = "pain-history"
    const val safetyAdvisory = "safety-advisory"
    const val glossary = "glossary"
    const val bodyDiagram = "body-diagram"

    const val shareRoutineTemplate = "share/routine/{id}"

    fun shareRoutine(routineId: Long) = "share/routine/$routineId"
}
