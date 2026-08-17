package com.lowerbackstretching.wear

import android.content.Context
import com.lowerbackstretching.core.model.WatchRoutine
import com.lowerbackstretching.wear.sync.WatchRoutineStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

/**
 * Manages routine loading for Wear OS, combining the bundled standalone routine
 * with any custom routines synced from the paired companion phone.
 */
object WatchContent {
    private val json = Json { ignoreUnknownKeys = true }

    fun loadDefaultRoutine(context: Context): WatchRoutine {
        val text = context.assets
            .open("watch_routine.json")
            .bufferedReader()
            .use { it.readText() }
        return json.decodeFromString(text)
    }

    /**
     * Backward-compatible helper that loads the default routine.
     */
    fun loadRoutine(context: Context): WatchRoutine = loadDefaultRoutine(context)

    /**
     * Observes all available routines: the default routine first, followed by
     * any custom routines synced from the phone.
     */
    fun observeAllRoutines(context: Context): Flow<List<WatchRoutine>> {
        WatchRoutineStorage.init(context)
        val defaultRoutine = loadDefaultRoutine(context)
        return WatchRoutineStorage.routinesFlow.map { synced ->
            listOf(defaultRoutine) + synced
        }
    }
}
