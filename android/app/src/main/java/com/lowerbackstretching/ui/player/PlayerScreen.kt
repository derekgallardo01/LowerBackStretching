package com.lowerbackstretching.ui.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Three entry composables for the player. Each loads a different kind of
 * stretch source into [PlayerViewModel], then delegates rendering to
 * [PlayerBody].
 */

@Composable
fun PlayerScreen(
    programId: String,
    dayNumber: Int,
    onFinished: () -> Unit,
    onBack: () -> Unit,
    vm: PlayerViewModel = viewModel(),
) {
    LaunchedEffect(programId, dayNumber) { vm.loadProgram(programId, dayNumber) }
    PlayerBody(title = "Day $dayNumber", onFinished = onFinished, onBack = onBack, vm = vm)
}

@Composable
fun SingleStretchPlayerScreen(
    stretchId: String,
    onFinished: () -> Unit,
    onBack: () -> Unit,
    vm: PlayerViewModel = viewModel(),
) {
    LaunchedEffect(stretchId) { vm.loadSingle(stretchId) }
    val title = vm.state.collectAsState().value?.current?.name ?: "Practice"
    PlayerBody(title = title, onFinished = onFinished, onBack = onBack, vm = vm)
}

@Composable
fun CustomRoutinePlayerScreen(
    routineId: Long,
    routineName: String,
    onFinished: () -> Unit,
    onBack: () -> Unit,
    vm: PlayerViewModel = viewModel(),
) {
    LaunchedEffect(routineId) { vm.loadCustomRoutine(routineId) }
    PlayerBody(title = routineName, onFinished = onFinished, onBack = onBack, vm = vm)
}
