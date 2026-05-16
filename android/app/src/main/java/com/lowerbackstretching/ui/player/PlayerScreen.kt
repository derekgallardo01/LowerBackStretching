package com.lowerbackstretching.ui.player

import android.app.Application
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.App
import com.lowerbackstretching.data.model.Stretch
import com.lowerbackstretching.ui.components.YouTubePlayerView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModel(app: Application) : AndroidViewModel(app) {
    private val appCtx: App get() = getApplication()

    private val _engine = MutableStateFlow<PlayerEngine?>(null)

    val state: StateFlow<PlayerEngine.Snapshot?> = _engine
        .flatMapLatest { it?.state ?: flowOf(null) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private var programId: String = ""
    private var dayNumber: Int = 1
    private var loaded: Boolean = false

    fun loadProgram(programId: String, dayNumber: Int) {
        if (loaded && this.programId == programId && this.dayNumber == dayNumber) return
        this.programId = programId
        this.dayNumber = dayNumber
        val program = appCtx.contentRepository.program(programId) ?: return
        initEngine(appCtx.contentRepository.stretchesFor(program, dayNumber))
    }

    fun loadSingle(stretchId: String) {
        val stretch = appCtx.contentRepository.stretch(stretchId) ?: return
        if (loaded && this.programId == "single-$stretchId") return
        this.programId = "single-$stretchId"
        this.dayNumber = 0
        initEngine(listOf(stretch))
    }

    fun loadCustomRoutine(routineId: Long) {
        val pid = "routine-$routineId"
        if (loaded && this.programId == pid) return
        viewModelScope.launch {
            val routine = appCtx.customRoutineRepository.byId(routineId) ?: return@launch
            val stretches = routine.stretchIds.mapNotNull { appCtx.contentRepository.stretch(it) }
            this@PlayerViewModel.programId = pid
            this@PlayerViewModel.dayNumber = 0
            initEngine(stretches)
        }
    }

    private fun initEngine(stretches: List<Stretch>) {
        loaded = true
        val engine = PlayerEngine(stretches)
        _engine.value = engine
        startTicker(engine)
    }

    private fun startTicker(engine: PlayerEngine) {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                val finishedNow = engine.tick()
                if (finishedNow) recordSession(engine.totalDurationSeconds)
            }
        }
    }

    fun togglePlay() = _engine.value?.togglePlay()

    fun next() {
        val engine = _engine.value ?: return
        val finishedNow = engine.next()
        if (finishedNow) recordSession(engine.totalDurationSeconds)
    }

    fun previous() = _engine.value?.previous()

    private fun recordSession(durationSeconds: Int) {
        viewModelScope.launch {
            appCtx.sessionRepository.recordCompletion(
                programId = programId,
                day = dayNumber,
                durationSeconds = durationSeconds,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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

@OptIn(ExperimentalMaterial3Api::class)
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

@OptIn(ExperimentalMaterial3Api::class)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerBody(
    title: String,
    onFinished: () -> Unit,
    onBack: () -> Unit,
    vm: PlayerViewModel,
) {
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        }
    ) { inner ->
        val snapshot = state ?: return@Scaffold
        val current = snapshot.current ?: return@Scaffold

        if (snapshot.finished) {
            FinishedView(modifier = Modifier.padding(inner).fillMaxSize(), onDone = onFinished)
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            YouTubePlayerView(videoId = current.youtubeId, modifier = Modifier.fillMaxWidth())

            Text(current.name, style = MaterialTheme.typography.headlineMedium)
            Text(current.description, style = MaterialTheme.typography.bodyMedium)

            LinearProgressIndicator(
                progress = { snapshot.progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp),
            )
            Text(
                "${snapshot.remainingSeconds}s · ${snapshot.index + 1} of ${snapshot.stretches.size}",
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = vm::previous) {
                    Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous", modifier = Modifier.size(36.dp))
                }
                Button(onClick = vm::togglePlay) {
                    Text(if (snapshot.running) "Pause" else "Resume")
                }
                IconButton(onClick = vm::next) {
                    Icon(Icons.Filled.SkipNext, contentDescription = "Next", modifier = Modifier.size(36.dp))
                }
            }
        }
    }
}

@Composable
private fun FinishedView(modifier: Modifier, onDone: () -> Unit) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Nice work.", style = MaterialTheme.typography.headlineLarge)
            Text("Session logged.", style = MaterialTheme.typography.bodyLarge)
            Button(onClick = onDone) { Text("Done") }
        }
    }
}
