package com.lowerbackstretching.ui.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.R

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
    PlayerBody(
        title = stringResource(R.string.player_day_title, dayNumber),
        onFinished = onFinished,
        onBack = onBack,
        vm = vm,
    )
}

@Composable
fun SingleStretchPlayerScreen(
    stretchId: String,
    onFinished: () -> Unit,
    onBack: () -> Unit,
    vm: PlayerViewModel = viewModel(),
) {
    LaunchedEffect(stretchId) { vm.loadSingle(stretchId) }
    val title = vm.state
        .collectAsState()
        .value
        ?.current
        ?.name ?: stringResource(R.string.player_practice_title)
    PlayerBody(title = title, onFinished = onFinished, onBack = onBack, vm = vm)
}

@Composable
fun CustomRoutinePlayerScreen(
    routineId: Long,
    onFinished: () -> Unit,
    onBack: () -> Unit,
    vm: PlayerViewModel = viewModel(),
) {
    LaunchedEffect(routineId) { vm.loadCustomRoutine(routineId) }
    // Falls back only for the frame or two before the routine row is read.
    val title = vm.routineName.collectAsState().value ?: stringResource(R.string.player_fallback_title)
    PlayerBody(title = title, onFinished = onFinished, onBack = onBack, vm = vm)
}
