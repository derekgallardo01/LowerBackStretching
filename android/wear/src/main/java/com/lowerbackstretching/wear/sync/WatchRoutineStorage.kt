package com.lowerbackstretching.wear.sync

import android.content.Context
import com.lowerbackstretching.core.model.WatchRoutine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

/**
 * Local persistent storage on Wear OS for routines synchronized from the phone.
 * Saves JSON directly into internal storage to ensure 100% offline availability.
 */
object WatchRoutineStorage {
    private const val FILE_NAME = "synced_routines.json"
    private val _routinesFlow = MutableStateFlow<List<WatchRoutine>>(emptyList())
    val routinesFlow: StateFlow<List<WatchRoutine>> = _routinesFlow.asStateFlow()
    private var initialized = false

    fun init(context: Context) {
        if (!initialized) {
            _routinesFlow.value = loadFromDisk(context)
            initialized = true
        }
    }

    fun saveRoutines(context: Context, routines: List<WatchRoutine>) {
        runCatching {
            val file = File(context.filesDir, FILE_NAME)
            val json = WatchRoutine.encodeList(routines)
            file.writeText(json)
            _routinesFlow.value = routines
        }
    }

    fun loadFromDisk(context: Context): List<WatchRoutine> {
        return runCatching {
            val file = File(context.filesDir, FILE_NAME)
            if (!file.exists()) emptyList()
            else WatchRoutine.decodeList(file.readText())
        }.getOrElse { emptyList() }
    }
}
