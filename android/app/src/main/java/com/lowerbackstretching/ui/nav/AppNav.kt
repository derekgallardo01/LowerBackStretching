package com.lowerbackstretching.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lowerbackstretching.calendar.scheduleStretchBreakIntent
import com.lowerbackstretching.data.Prefs
import com.lowerbackstretching.ui.achievements.AchievementsScreen
import com.lowerbackstretching.ui.anatomy.BodyDiagramScreen
import com.lowerbackstretching.ui.calendar.CalendarScreen
import com.lowerbackstretching.ui.flexibility.FlexibilityScreen
import com.lowerbackstretching.ui.goals.GoalsScreen
import com.lowerbackstretching.ui.home.HomeAction
import com.lowerbackstretching.ui.home.HomeScreen
import com.lowerbackstretching.ui.learn.GlossaryScreen
import com.lowerbackstretching.ui.onboarding.OnboardingScreen
import com.lowerbackstretching.ui.player.CustomRoutinePlayerScreen
import com.lowerbackstretching.ui.player.PlayerScreen
import com.lowerbackstretching.ui.player.SingleStretchPlayerScreen
import com.lowerbackstretching.ui.programs.ProgramDetailScreen
import com.lowerbackstretching.ui.programs.ProgramsScreen
import com.lowerbackstretching.share.SharedRoutine
import com.lowerbackstretching.ui.routines.RoutineBuilderScreen
import com.lowerbackstretching.ui.settings.SettingsScreen
import com.lowerbackstretching.ui.share.ImportRoutineSheet
import com.lowerbackstretching.ui.share.ShareRoutineScreen
import com.lowerbackstretching.ui.stretches.StretchDetailScreen
import com.lowerbackstretching.ui.stretches.StretchesScreen

@Composable
fun AppNav(
    pendingImport: SharedRoutine? = null,
    onConsumeImport: () -> Unit = {},
) {
    val ctx = LocalContext.current
    val prefs = remember(ctx) { Prefs(ctx) }
    val onboardingDone by prefs.onboardingDone.collectAsState(initial = null)

    when (onboardingDone) {
        null -> Unit
        false -> OnboardingScreen(onDone = { /* DataStore flow triggers recomposition */ })
        true -> AppRoot(pendingImport = pendingImport, onConsumeImport = onConsumeImport)
    }
}

@Composable
private fun AppRoot(
    pendingImport: SharedRoutine? = null,
    onConsumeImport: () -> Unit = {},
) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val currentRoute = entry?.destination?.route

    Scaffold(
        bottomBar = {
            if (Tab.all.any { it.path == currentRoute }) {
                NavigationBar {
                    Tab.all.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.path,
                            onClick = {
                                if (currentRoute != tab.path) {
                                    nav.navigate(tab.path) {
                                        popUpTo(Tab.Home.path) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        }
    ) { inner ->
        NavHost(
            navController = nav,
            startDestination = Tab.Home.path,
            modifier = Modifier.padding(inner),
        ) {
            composable(Tab.Home.path) {
                val ctx = LocalContext.current
                HomeScreen(onAction = { action ->
                    when (action) {
                        is HomeAction.OpenProgram -> nav.navigate(Dest.program(action.id))
                        HomeAction.OpenAchievements -> nav.navigate(Dest.achievements)
                        HomeAction.OpenGoals -> nav.navigate(Dest.goals)
                        HomeAction.OpenFlexibility -> nav.navigate(Dest.flexibility)
                        HomeAction.OpenGlossary -> nav.navigate(Dest.glossary)
                        HomeAction.OpenBodyDiagram -> nav.navigate(Dest.bodyDiagram)
                        HomeAction.ScheduleBreak -> ctx.startActivity(scheduleStretchBreakIntent())
                    }
                })
            }
            composable(Dest.bodyDiagram) {
                BodyDiagramScreen(
                    onOpenStretch = { id -> nav.navigate(Dest.stretch(id)) },
                    onBack = { nav.popBackStack() },
                )
            }
            composable(Dest.achievements) {
                AchievementsScreen(onBack = { nav.popBackStack() })
            }
            composable(Dest.goals) {
                GoalsScreen(onBack = { nav.popBackStack() })
            }
            composable(Dest.flexibility) {
                FlexibilityScreen(onBack = { nav.popBackStack() })
            }
            composable(Dest.glossary) {
                GlossaryScreen(onBack = { nav.popBackStack() })
            }
            composable(Tab.Programs.path) {
                ProgramsScreen(
                    onOpenProgram = { id -> nav.navigate(Dest.program(id)) },
                    onOpenCustomRoutine = { rid -> nav.navigate(Dest.routinePlayer(rid)) },
                    onCreateRoutine = { nav.navigate(Dest.routineNew) },
                    onShareRoutine = { rid -> nav.navigate(Dest.shareRoutine(rid)) },
                )
            }
            composable(Dest.shareRoutineTemplate) { backStack ->
                val id = backStack.arguments?.getString("id")?.toLongOrNull() ?: return@composable
                ShareRoutineScreen(routineId = id, onBack = { nav.popBackStack() })
            }
            composable(Dest.routineNew) {
                RoutineBuilderScreen(
                    onSaved = { nav.popBackStack() },
                    onBack = { nav.popBackStack() },
                )
            }
            composable(Dest.routinePlayerTemplate) { backStack ->
                val id = backStack.arguments?.getString("id")?.toLongOrNull() ?: return@composable
                CustomRoutinePlayerScreen(
                    routineId = id,
                    routineName = "Routine",
                    onFinished = { nav.popBackStack(route = Tab.Programs.path, inclusive = false) },
                    onBack = { nav.popBackStack() },
                )
            }
            composable(Dest.programTemplate) { backStack ->
                val id = backStack.arguments?.getString("id").orEmpty()
                ProgramDetailScreen(
                    programId = id,
                    onStartDay = { day -> nav.navigate(Dest.player(id, day)) },
                    onBack = { nav.popBackStack() },
                )
            }
            composable(Dest.playerTemplate) { backStack ->
                val id = backStack.arguments?.getString("id").orEmpty()
                val day = backStack.arguments?.getString("day")?.toIntOrNull() ?: 1
                PlayerScreen(
                    programId = id,
                    dayNumber = day,
                    // After finishing, return all the way to the Programs tab
                    // so the bottom bar is visible. Pops the player AND the
                    // program-detail screen.
                    onFinished = { nav.popBackStack(route = Tab.Programs.path, inclusive = false) },
                    onBack = { nav.popBackStack() },
                )
            }
            composable(Tab.Stretches.path) {
                StretchesScreen(onOpenStretch = { id -> nav.navigate(Dest.stretch(id)) })
            }
            composable(Dest.stretchTemplate) { backStack ->
                val id = backStack.arguments?.getString("id").orEmpty()
                StretchDetailScreen(
                    stretchId = id,
                    onPractice = { nav.navigate(Dest.singlePlayer(id)) },
                    onBack = { nav.popBackStack() },
                )
            }
            composable(Dest.singlePlayerTemplate) { backStack ->
                val id = backStack.arguments?.getString("id").orEmpty()
                SingleStretchPlayerScreen(
                    stretchId = id,
                    onFinished = { nav.popBackStack() },
                    onBack = { nav.popBackStack() },
                )
            }
            composable(Tab.Calendar.path) { CalendarScreen() }
            composable(Tab.Settings.path) { SettingsScreen() }
        }
    }

    if (pendingImport != null) {
        ImportRoutineSheet(routine = pendingImport, onDismiss = onConsumeImport)
    }
}
