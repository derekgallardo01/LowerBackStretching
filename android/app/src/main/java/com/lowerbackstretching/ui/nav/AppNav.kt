package com.lowerbackstretching.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lowerbackstretching.ui.calendar.CalendarScreen
import com.lowerbackstretching.ui.home.HomeScreen
import com.lowerbackstretching.ui.onboarding.OnboardingScreen
import com.lowerbackstretching.ui.player.CustomRoutinePlayerScreen
import com.lowerbackstretching.ui.player.PlayerScreen
import com.lowerbackstretching.ui.player.SingleStretchPlayerScreen
import com.lowerbackstretching.ui.routines.RoutineBuilderScreen
import com.lowerbackstretching.ui.programs.ProgramDetailScreen
import com.lowerbackstretching.ui.programs.ProgramsScreen
import com.lowerbackstretching.ui.settings.SettingsScreen
import com.lowerbackstretching.ui.stretches.StretchDetailScreen
import com.lowerbackstretching.ui.stretches.StretchesScreen
import com.lowerbackstretching.data.Prefs
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext

sealed class Route(val path: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    data object Home      : Route("home", "Home", Icons.Filled.Home)
    data object Programs  : Route("programs", "Programs", Icons.Filled.FitnessCenter)
    data object Stretches : Route("stretches", "Stretches", Icons.Filled.SelfImprovement)
    data object Calendar  : Route("calendar", "Calendar", Icons.Filled.CalendarMonth)
    data object Settings  : Route("settings", "Settings", Icons.Filled.Settings)
}

private val BottomTabs = listOf(Route.Home, Route.Programs, Route.Stretches, Route.Calendar, Route.Settings)

@Composable
fun AppNav() {
    val ctx = LocalContext.current
    val prefs = remember(ctx) { Prefs(ctx) }
    val onboardingDone by prefs.onboardingDone.collectAsState(initial = null)

    when (onboardingDone) {
        null -> Unit
        false -> OnboardingScreen(onDone = { /* DataStore flow triggers recomposition */ })
        true -> AppRoot()
    }
}

@Composable
private fun AppRoot() {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val currentRoute = entry?.destination?.route

    Scaffold(
        bottomBar = {
            if (BottomTabs.any { it.path == currentRoute }) {
                NavigationBar {
                    BottomTabs.forEach { route ->
                        NavigationBarItem(
                            selected = currentRoute == route.path,
                            onClick = {
                                if (currentRoute != route.path) {
                                    nav.navigate(route.path) {
                                        popUpTo(Route.Home.path) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(route.icon, contentDescription = route.label) },
                            label = { Text(route.label) },
                        )
                    }
                }
            }
        }
    ) { inner ->
        NavHost(
            navController = nav,
            startDestination = Route.Home.path,
            modifier = androidx.compose.ui.Modifier.padding(inner),
        ) {
            composable(Route.Home.path) {
                HomeScreen(
                    onOpenPrograms = { nav.navigate(Route.Programs.path) },
                    onOpenProgram = { id -> nav.navigate("program/$id") },
                )
            }
            composable(Route.Programs.path) {
                ProgramsScreen(
                    onOpenProgram = { id -> nav.navigate("program/$id") },
                    onOpenCustomRoutine = { rid -> nav.navigate("routine/$rid/play") },
                    onCreateRoutine = { nav.navigate("routine/new") },
                )
            }
            composable("routine/new") {
                RoutineBuilderScreen(
                    onSaved = { nav.popBackStack() },
                    onBack = { nav.popBackStack() },
                )
            }
            composable("routine/{id}/play") { backStack ->
                val id = backStack.arguments?.getString("id")?.toLongOrNull() ?: return@composable
                CustomRoutinePlayerScreen(
                    routineId = id,
                    routineName = "Routine",
                    onFinished = { nav.popBackStack(route = Route.Programs.path, inclusive = false) },
                    onBack = { nav.popBackStack() },
                )
            }
            composable("program/{id}") { backStack ->
                val id = backStack.arguments?.getString("id").orEmpty()
                ProgramDetailScreen(
                    programId = id,
                    onStartDay = { day -> nav.navigate("player/$id/$day") },
                    onBack = { nav.popBackStack() },
                )
            }
            composable("player/{id}/{day}") { backStack ->
                val id = backStack.arguments?.getString("id").orEmpty()
                val day = backStack.arguments?.getString("day")?.toIntOrNull() ?: 1
                PlayerScreen(
                    programId = id,
                    dayNumber = day,
                    onFinished = {
                        nav.popBackStack(route = "program/$id", inclusive = false)
                    },
                    onBack = { nav.popBackStack() },
                )
            }
            composable(Route.Stretches.path) {
                StretchesScreen(onOpenStretch = { id -> nav.navigate("stretch/$id") })
            }
            composable("stretch/{id}") { backStack ->
                val id = backStack.arguments?.getString("id").orEmpty()
                StretchDetailScreen(
                    stretchId = id,
                    onPractice = { nav.navigate("player/single/$id") },
                    onBack = { nav.popBackStack() },
                )
            }
            composable("player/single/{id}") { backStack ->
                val id = backStack.arguments?.getString("id").orEmpty()
                SingleStretchPlayerScreen(
                    stretchId = id,
                    onFinished = { nav.popBackStack() },
                    onBack = { nav.popBackStack() },
                )
            }
            composable(Route.Calendar.path) { CalendarScreen() }
            composable(Route.Settings.path) { SettingsScreen() }
        }
    }
}
