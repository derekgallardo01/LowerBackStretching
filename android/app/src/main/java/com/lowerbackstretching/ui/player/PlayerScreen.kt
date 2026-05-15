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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlayerState(
    val stretches: List<Stretch> = emptyList(),
    val index: Int = 0,
    val remainingSeconds: Int = 0,
    val running: Boolean = false,
    val finished: Boolean = false,
) {
    val current: Stretch? get() = stretches.getOrNull(index)
    val progress: Float
        get() = current?.let {
            if (it.durationSeconds == 0) 0f
            else (it.durationSeconds - remainingSeconds).toFloat() / it.durationSeconds
        } ?: 0f
}

class PlayerViewModel(app: Application) : AndroidViewModel(app) {
    private val appCtx: App get() = getApplication()
    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private var programId: String = ""
    private var dayNumber: Int = 1
    private var loaded: Boolean = false

    fun loadProgram(programId: String, dayNumber: Int) {
        if (loaded && this.programId == programId && this.dayNumber == dayNumber) return
        this.programId = programId
        this.dayNumber = dayNumber
        val program = appCtx.contentRepository.program(programId) ?: return
        initState(appCtx.contentRepository.stretchesFor(program, dayNumber))
    }

    fun loadSingle(stretchId: String) {
        val stretch = appCtx.contentRepository.stretch(stretchId) ?: return
        if (loaded && this.programId == "single-$stretchId") return
        this.programId = "single-$stretchId"
        this.dayNumber = 0
        initState(listOf(stretch))
    }

    private fun initState(stretches: List<Stretch>) {
        loaded = true
        _state.value = PlayerState(
            stretches = stretches,
            index = 0,
            remainingSeconds = stretches.firstOrNull()?.durationSeconds ?: 0,
            running = true,
        )
        startTicker()
    }

    private fun startTicker() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                val s = _state.value
                if (!s.running || s.finished) continue
                if (s.remainingSeconds > 1) {
                    _state.update { it.copy(remainingSeconds = it.remainingSeconds - 1) }
                } else {
                    next()
                }
            }
        }
    }

    fun togglePlay() = _state.update { it.copy(running = !it.running) }

    fun next() {
        val s = _state.value
        val nextIdx = s.index + 1
        if (nextIdx >= s.stretches.size) {
            _state.update { it.copy(finished = true, running = false) }
            viewModelScope.launch {
                appCtx.sessionRepository.recordCompletion(
                    programId = programId,
                    day = dayNumber,
                    durationSeconds = s.stretches.sumOf { it.durationSeconds },
                )
            }
        } else {
            _state.update {
                it.copy(index = nextIdx, remainingSeconds = it.stretches[nextIdx].durationSeconds)
            }
        }
    }

    fun previous() {
        val s = _state.value
        val prevIdx = (s.index - 1).coerceAtLeast(0)
        _state.update {
            it.copy(index = prevIdx, remainingSeconds = it.stretches[prevIdx].durationSeconds, finished = false)
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
    val title = vm.state.collectAsState().value.current?.name ?: "Practice"
    PlayerBody(title = title, onFinished = onFinished, onBack = onBack, vm = vm)
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
    val current = state.current

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
        if (current == null) return@Scaffold

        if (state.finished) {
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
                progress = { state.progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp),
            )
            Text(
                "${state.remainingSeconds}s · ${state.index + 1} of ${state.stretches.size}",
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
                    Text(if (state.running) "Pause" else "Resume")
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
